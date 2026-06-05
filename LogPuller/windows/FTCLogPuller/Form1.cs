using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace FTCLogPuller
{
    public partial class Form1 : Form
    {
        [DllImport("user32.dll")] static extern bool SetForegroundWindow(IntPtr hWnd);

        // ── State ────────────────────────────────────────────────────────────
        string _destPath = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        string _adbPath  = "";
        CancellationTokenSource? _cts;
        bool _running = false;
        int _pulled = 0, _skipped = 0, _totalFiles = 0;

        // ── Controls ─────────────────────────────────────────────────────────
        Label        _lblTitle        = null!;
        Label        _lblVersion      = null!;
        Panel        _headerPanel     = null!;
        TextBox      _txtDestPath     = null!;
        Button       _btnBrowse       = null!;
        Panel        _deviceStatusDot = null!;
        Label        _lblDeviceName   = null!;
        Label        _lblDeviceState  = null!;
        Button       _btnRefreshDev   = null!;
        RichTextBox  _rtbLog          = null!;
        ProgressBar  _progressBar     = null!;
        Label        _lblProgress     = null!;
        Label        _lblPulled       = null!;
        Label        _lblSkipped      = null!;
        Label        _lblTotal        = null!;
        Button       _btnPull         = null!;
        Button       _btnOpen         = null!;
        CheckBox     _chkDelete       = null!;

        // ── Colors ───────────────────────────────────────────────────────────
        static readonly Color ColBg       = Color.FromArgb(245, 245, 245);
        static readonly Color ColPanel    = Color.White;
        static readonly Color ColBorder   = Color.FromArgb(220, 220, 220);
        static readonly Color ColText     = Color.FromArgb(30, 30, 30);
        static readonly Color ColMuted    = Color.FromArgb(110, 110, 110);
        static readonly Color ColGreen    = Color.FromArgb(29, 158, 117);
        static readonly Color ColGreenHov = Color.FromArgb(15, 110, 86);
        static readonly Color ColAmber    = Color.FromArgb(186, 117, 23);
        static readonly Color ColLogBg    = Color.FromArgb(30, 30, 30);

        public Form1()
        {
            InitializeComponent();
            BuildUI();
            // Kick off ADB check after form is shown so download prompt works
            Load += async (s, e) => await InitAdb();
        }

        // ── UI Builder ───────────────────────────────────────────────────────
        void BuildUI()
        {
            AutoScaleMode = AutoScaleMode.Dpi;

            Text            = "FTC Log Puller";
            Size            = new Size(560, 660);
            MinimumSize     = new Size(460, 560);
            BackColor       = ColBg;
            Font            = new Font("Segoe UI", 9.5f);
            FormBorderStyle = FormBorderStyle.FixedSingle;
            MaximizeBox     = false;
            StartPosition   = FormStartPosition.CenterScreen;

            // ── Header ──────────────────────────────────────────────────────
            _headerPanel = new Panel
            {
                Dock = DockStyle.Top,
                Height = 70,
                BackColor = ColPanel
            };

            _headerPanel.Paint += (s, e) =>
                e.Graphics.DrawLine(
                    new Pen(ColBorder, 1),
                    0,
                    _headerPanel.Height - 1,
                    _headerPanel.Width,
                    _headerPanel.Height - 1);

            _lblTitle = new Label
            {
                Text = "FTC Log Puller",
                Font = new Font("Segoe UI Semibold", 13f),
                ForeColor = ColText,
                AutoSize = true,
                Location = new Point(16, 18)
            };

            _lblVersion = new Label
            {
                Text = "v2.0",
                Font = new Font("Segoe UI", 8.5f),
                ForeColor = ColMuted,
                AutoSize = true,
                Location = new Point(16 + _lblTitle.PreferredWidth + 6, 24)
            };

            _headerPanel.Controls.AddRange(new Control[]
            {
                _lblTitle,
                _lblVersion
            });

            Controls.Add(_headerPanel);

            // ── Scroll panel ────────────────────────────────────────────────
            var scroll = new Panel
            {
                Dock = DockStyle.Fill,
                BackColor = ColBg,
                AutoScroll = true
            };

            Controls.Add(scroll);

            int y = 24;
            int pad = 16;

            // ── Save location ───────────────────────────────────────────────
            scroll.Controls.Add(SectionLabel("Save location", pad, y));
            y += 50;

            var pathRow = new TableLayoutPanel
            {
                Left = pad,
                Top = y,
                Height = 38,
                ColumnCount = 2,
                RowCount = 1,
                BackColor = Color.Transparent,
                Padding = new Padding(0),
                Margin = new Padding(0),
                CellBorderStyle = TableLayoutPanelCellBorderStyle.None,
                Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right
            };

            pathRow.ColumnStyles.Add(
                new ColumnStyle(SizeType.Percent, 100f));

            pathRow.ColumnStyles.Add(
                new ColumnStyle(SizeType.Absolute, 100f));

            _txtDestPath = new TextBox
            {
                Dock = DockStyle.Fill,
                ReadOnly = true,
                BackColor = Color.White,
                ForeColor = ColText,
                BorderStyle = BorderStyle.FixedSingle,
                Font = new Font("Segoe UI", 9.5f),
                Text = _destPath,
                Margin = new Padding(0, 0, 4, 0)
            };

            _btnBrowse = new Button
            {
                Text = "Browse...",
                Dock = DockStyle.Fill,
                FlatStyle = FlatStyle.Flat,
                BackColor = Color.White,
                ForeColor = ColText,
                Font = new Font("Segoe UI", 9f),
                Cursor = Cursors.Hand,
                Margin = new Padding(0, 2, 0, 2)
            };

            _btnBrowse.FlatAppearance.BorderColor = ColBorder;
            _btnBrowse.FlatAppearance.BorderSize = 1;
            _btnBrowse.Click += (s, e) => BrowseDestination();

            pathRow.Controls.Add(_txtDestPath, 0, 0);
            pathRow.Controls.Add(_btnBrowse, 1, 0);

            scroll.Controls.Add(pathRow);

            y += 48;

            // ── ADB Device ──────────────────────────────────────────────────
            scroll.Controls.Add(SectionLabel("ADB device", pad, y));
            y += 22;

            var devPanel = new Panel
            {
                Left = pad,
                Top = y,
                Height = 38,
                BackColor = ColPanel,
                BorderStyle = BorderStyle.FixedSingle
            };

            _deviceStatusDot = new Panel
            {
                Size = new Size(10, 10),
                Location = new Point(10, 14),
                BackColor = ColMuted
            };

            _deviceStatusDot.Paint += (s, e) =>
            {
                e.Graphics.SmoothingMode =
                    System.Drawing.Drawing2D.SmoothingMode.AntiAlias;

                using var br =
                    new SolidBrush(_deviceStatusDot.BackColor);

                e.Graphics.FillEllipse(br, 0, 0, 9, 9);
            };

            _lblDeviceName = new Label
            {
                AutoSize = true,
                Location = new Point(28, 11),
                ForeColor = ColText,
                Font = new Font("Segoe UI", 9.5f),
                Text = "Initialising…"
            };

            _lblDeviceState = new Label
            {
                AutoSize = true,
                ForeColor = ColMuted,
                Font = new Font("Segoe UI", 9f),
                Text = ""
            };

            _btnRefreshDev = new Button
            {
                Text = "↺",
                Width = 30,
                Height = 30,
                FlatStyle = FlatStyle.Flat,
                BackColor = Color.White,
                ForeColor = ColText,
                Font = new Font("Segoe UI", 11f),
                Cursor = Cursors.Hand
            };

            _btnRefreshDev.FlatAppearance.BorderSize = 0;
            _btnRefreshDev.Click += (s, e) => RefreshDevice();

            devPanel.Controls.AddRange(new Control[]
            {
                _deviceStatusDot,
                _lblDeviceName,
                _lblDeviceState,
                _btnRefreshDev
            });

            scroll.Controls.Add(devPanel);

            y += 50;

            // ── Options ─────────────────────────────────────────────────────
            scroll.Controls.Add(SectionLabel("Options", pad, y));
            y += 22;

            _chkDelete = new CheckBox
            {
                Text = "Delete logs from device after pulling",
                Left = pad,
                Top = y,
                Width = 320,
                Height = 22,
                Checked = true,
                ForeColor = ColText
            };

            scroll.Controls.Add(_chkDelete);

            y += 32;

            // ── Log ─────────────────────────────────────────────────────────
            scroll.Controls.Add(SectionLabel("Log", pad, y));
            y += 22;

            _rtbLog = new RichTextBox
            {
                Left = pad,
                Top = y,
                Height = 150,
                BackColor = ColLogBg,
                ForeColor = Color.FromArgb(180, 180, 180),
                Font = new Font("Consolas", 8.5f),
                ReadOnly = true,
                BorderStyle = BorderStyle.FixedSingle,
                ScrollBars = RichTextBoxScrollBars.Vertical,
                WordWrap = false
            };

            scroll.Controls.Add(_rtbLog);

            y += 160;

            // ── Progress ─────────────────────────────────────────────────────
            var progLabel = SectionLabel("Progress", pad, y);
            scroll.Controls.Add(progLabel);
            _lblProgress = new Label {
                AutoSize = true, ForeColor = ColMuted,
                Font = new Font("Segoe UI", 8.5f), Text = "0 / 0"
            };
            scroll.Controls.Add(_lblProgress);
            y += 22;
            _progressBar = new ProgressBar {
                Left = pad, Top = y, Height = 6,
                Style = ProgressBarStyle.Continuous,
                Minimum = 0, Maximum = 100, Value = 0,
                ForeColor = ColGreen, BackColor = Color.FromArgb(220, 220, 220)
            };
            scroll.Controls.Add(_progressBar);
            y += 18;

            // ── Stats ────────────────────────────────────────────────────────
            y += 10;
            (_lblPulled,  _) = StatCard(scroll, 0, y, "0", "Pulled",       ColGreen);
            (_lblSkipped, _) = StatCard(scroll, 1, y, "0", "Already downloaded",  ColAmber);
            (_lblTotal,   _) = StatCard(scroll, 2, y, "0", "Total",        ColMuted);
            y += 72;

            // ── Action buttons ───────────────────────────────────────────────
            y += 6;
            _btnPull = new Button {
                Left = pad, Top = y, Height = 40,
                Text = "⬇  Pull logs", FlatStyle = FlatStyle.Flat,
                BackColor = ColGreen, ForeColor = Color.White,
                Font = new Font("Segoe UI Semibold", 10.5f), Cursor = Cursors.Hand
            };
            _btnPull.FlatAppearance.BorderSize = 0;
            _btnPull.Click += async (s, e) => await RunPull();
            _btnPull.MouseEnter += (s, e) => { if (_btnPull.Enabled) _btnPull.BackColor = ColGreenHov; };
            _btnPull.MouseLeave += (s, e) => { if (_btnPull.Enabled) _btnPull.BackColor = ColGreen; };

            _btnOpen = new Button {
                Top = y, Width = 130, Height = 40,
                Text = "📂 Open saved logs", FlatStyle = FlatStyle.Flat,
                BackColor = ColPanel, ForeColor = ColText,
                Font = new Font("Segoe UI", 9f), Cursor = Cursors.Hand
            };
            _btnOpen.FlatAppearance.BorderColor = ColBorder;
            _btnOpen.FlatAppearance.BorderSize  = 1;
            _btnOpen.Click += (s, e) => OpenDestFolder();

            scroll.Controls.Add(_btnPull);
            scroll.Controls.Add(_btnOpen);

            // ── Layout on resize ─────────────────────────────────────────────
            void DoLayout()
            {
                int w = scroll.ClientSize.Width - pad * 2;
                if (w <= 0) return;

                pathRow.Width      = w;
                devPanel.Width     = w;
                _rtbLog.Width      = w;
                _progressBar.Width = w;

                // Progress label right-aligned
                _lblProgress.Location = new Point(pad + w - _lblProgress.PreferredWidth, progLabel.Top);

                // Refresh button right-aligned inside device panel
                _btnRefreshDev.Location = new Point(devPanel.Width - _btnRefreshDev.Width - 4, 4);

                // Device state label right of name
                _lblDeviceState.Location = new Point(devPanel.Width - _btnRefreshDev.Width - _lblDeviceState.Width - 12, 11);

                // Stat cards — equal thirds
                int cardGap = 10;
                int cardW   = (w - cardGap * 2) / 3;
                for (int i = 0; i < 3; i++)
                {
                    var card = scroll.Controls[$"statcard_{i}"] as Panel;
                    if (card == null) continue;
                    card.Left  = pad + i * (cardW + cardGap);
                    card.Width = cardW;
                    // Fix inner label widths too
                    foreach (Control c in card.Controls) c.Width = cardW - 4;
                }

                // Pull + Open buttons
                int openW   = 130;
                int pullW   = w - openW - cardGap;
                _btnPull.Width = pullW;
                _btnOpen.Left  = pad + pullW + cardGap;
            }

            scroll.SizeChanged += (s, e) => DoLayout();
            // Also run once after form loads
            Load += (s, e) => DoLayout();
        }

        // ── Stat card helper ─────────────────────────────────────────────────
        (Label num, Label cap) StatCard(Panel parent, int slot, int y, string num, string cap, Color col)
        {
            var card = new Panel {
                Name = $"statcard_{slot}",
                Top = y, Height = 62,
                BackColor = ColPanel, BorderStyle = BorderStyle.FixedSingle
            };
            var lNum = new Label {
                Text = num, Font = new Font("Segoe UI Semibold", 18f), ForeColor = col,
                Height = 36, Top = 8, Left = 0, TextAlign = ContentAlignment.MiddleCenter,
                AutoSize = false
            };
            var lCap = new Label {
                Text = cap, Font = new Font("Segoe UI", 8.5f), ForeColor = ColMuted,
                Height = 16, Top = 42, Left = 0, TextAlign = ContentAlignment.MiddleCenter,
                AutoSize = false
            };
            card.Controls.Add(lNum);
            card.Controls.Add(lCap);
            parent.Controls.Add(card);
            return (lNum, lCap);
        }

        Label SectionLabel(string text, int x, int y) => new Label {
            Text = text, Left = x, Top = y, AutoSize = true,
            Font = new Font("Segoe UI Semibold", 8.5f), ForeColor = ColMuted
        };

        // ── ADB initialisation (runs on Load) ────────────────────────────────
        async Task InitAdb()
        {
            // Check local adb folder, exe dir, and PATH
            string exeDir = AppContext.BaseDirectory;
            string[] candidates = {
                Path.Combine(exeDir, "adb", "adb.exe"),
                Path.Combine(exeDir, "adb.exe"),
            };
            foreach (var c in candidates)
                if (File.Exists(c)) { _adbPath = c; RefreshDevice(); return; }

            string? onPath = FindOnPath("adb.exe");
            if (onPath != null) { _adbPath = onPath; RefreshDevice(); return; }

            // Not found — download automatically
            SetDevice(false, "ADB not found — downloading…", "");
            LogInfo("ADB not found. Downloading Android Platform Tools…");
            await DownloadAdb();
        }

        string? FindOnPath(string exe)
        {
            foreach (string dir in (Environment.GetEnvironmentVariable("PATH") ?? "").Split(';'))
            {
                string full = Path.Combine(dir.Trim(), exe);
                if (File.Exists(full)) return full;
            }
            return null;
        }

        // ── Device Refresh ───────────────────────────────────────────────────
        void RefreshDevice()
        {
            if (string.IsNullOrEmpty(_adbPath)) { SetDevice(false, "ADB not found", ""); return; }
            SetDevice(false, "Scanning…", "");
            Task.Run(() =>
            {
                try
                {
                    var psi = new ProcessStartInfo(_adbPath, "devices") {
                        RedirectStandardOutput = true, UseShellExecute = false, CreateNoWindow = true
                    };
                    using var p = Process.Start(psi)!;
                    string output = p.StandardOutput.ReadToEnd();
                    p.WaitForExit();
                    string? deviceLine = null;
                    foreach (var line in output.Split('\n'))
                    {
                        string t = line.Trim();
                        if (!string.IsNullOrEmpty(t) && !t.StartsWith("List") && t.Contains('\t'))
                        { deviceLine = t; break; }
                    }
                    if (deviceLine != null)
                        Invoke(() => SetDevice(true, deviceLine.Split('\t')[0].Trim(), "Connected"));
                    else
                        Invoke(() => SetDevice(false, "No device found", ""));
                }
                catch { Invoke(() => SetDevice(false, "ADB error", "")); }
            });
        }

        void SetDevice(bool online, string name, string state)
        {
            _deviceStatusDot.BackColor = online ? ColGreen : ColMuted;
            _deviceStatusDot.Invalidate();
            _lblDeviceName.Text  = name;
            _lblDeviceState.Text = state;
        }

        // ── Browse ───────────────────────────────────────────────────────────
        void BrowseDestination()
        {
            using var dlg = new FolderBrowserDialog {
                Description = "Choose where to save .wpilog files", SelectedPath = _destPath
            };
            if (dlg.ShowDialog(this) == DialogResult.OK)
            {
                _destPath        = dlg.SelectedPath;
                _txtDestPath.Text = _destPath;
            }
        }

        void OpenDestFolder()
        {
            if (Directory.Exists(_destPath)) Process.Start("explorer.exe", _destPath);
        }

        // ── Logging ──────────────────────────────────────────────────────────
        void Log(string msg, Color? col = null)
        {
            if (_rtbLog.InvokeRequired) { _rtbLog.Invoke(() => Log(msg, col)); return; }
            _rtbLog.SelectionStart  = _rtbLog.TextLength;
            _rtbLog.SelectionLength = 0;
            _rtbLog.SelectionColor  = Color.FromArgb(80, 80, 80);
            _rtbLog.AppendText(DateTime.Now.ToString("HH:mm:ss") + "  ");
            _rtbLog.SelectionColor  = col ?? Color.FromArgb(180, 180, 180);
            _rtbLog.AppendText(msg + "\n");
            _rtbLog.ScrollToCaret();
        }
        void LogInfo(string msg)    => Log(msg);
        void LogSuccess(string msg) => Log(msg, Color.FromArgb(80, 200, 140));
        void LogWarn(string msg)    => Log(msg, Color.FromArgb(220, 170, 60));
        void LogError(string msg)   => Log(msg, Color.FromArgb(220, 80, 80));

        // ── Progress ─────────────────────────────────────────────────────────
        void SetProgress(int done, int total)
        {
            if (InvokeRequired) { Invoke(() => SetProgress(done, total)); return; }
            _progressBar.Value = total > 0 ? Math.Min((int)((double)done / total * 100), 100) : 0;
            _lblProgress.Text  = $"{done} / {total}";
        }

        void UpdateStats()
        {
            if (InvokeRequired) { Invoke(UpdateStats); return; }
            _lblPulled.Text  = _pulled.ToString();
            _lblSkipped.Text = _skipped.ToString();
            _lblTotal.Text   = _totalFiles.ToString();
        }

        // ── ADB runner ───────────────────────────────────────────────────────
        async Task<string> RunAdb(string args)
        {
            var psi = new ProcessStartInfo(_adbPath, args) {
                RedirectStandardOutput = true, RedirectStandardError = true,
                UseShellExecute = false, CreateNoWindow = true
            };
            using var p = new Process { StartInfo = psi };
            p.Start();
            string output = await p.StandardOutput.ReadToEndAsync();
            await p.WaitForExitAsync();
            return output.Trim();
        }

        // ── Pull logic ───────────────────────────────────────────────────────
        async Task RunPull()
        {
            if (_running) { CancelPull(); return; }
            if (string.IsNullOrEmpty(_adbPath) || !File.Exists(_adbPath))
            {
                LogError("ADB not ready. Please wait for download to complete.");
                return;
            }

            _running = true;
            _cts     = new CancellationTokenSource();
            _pulled  = 0; _skipped = 0; _totalFiles = 0;
            UpdateStats();
            SetProgress(0, 0);
            _btnPull.Text      = "✕  Cancel";
            _btnPull.BackColor = Color.FromArgb(160, 40, 40);

            try
            {
                LogInfo("Scanning device for .wpilog files…");
                string output = await RunAdb("shell find /sdcard/Android/data -type f -name '*.wpilog' 2>/dev/null");

                if (string.IsNullOrWhiteSpace(output) || output.Contains("no such file") || output.Contains("Permission denied"))
                { LogWarn("No .wpilog files found on device."); return; }

                var files = new List<string>();
                foreach (var line in output.Split('\n'))
                {
                    string f = line.Trim().Replace("\r", "");
                    if (!string.IsNullOrEmpty(f) && f.EndsWith(".wpilog")) files.Add(f);
                }
                if (files.Count == 0) { LogWarn("No .wpilog files found."); return; }

                _totalFiles = files.Count;
                LogInfo($"Found {files.Count} file(s).");
                UpdateStats();

                bool deleteAfter = _chkDelete.Checked;
                int done = 0;

                foreach (var remote in files)
                {
                    if (_cts.Token.IsCancellationRequested) { LogWarn("Cancelled."); break; }

                    string fileName  = Path.GetFileName(remote);
                    string localFile = Path.Combine(_destPath, fileName);

                    if (File.Exists(localFile))
                    {
                        LogWarn($"Already downloaded: {fileName}");
                        _skipped++;
                        if (deleteAfter)
                        {
                            await RunAdb($"shell rm '{remote}'");
                            LogInfo($"  Deleted from device: {fileName}");
                        }
                    }
                    else
                    {
                        LogInfo($"Pulling: {fileName}");
                        await RunAdb($"pull \"{remote}\" \"{localFile}\"");
                        if (File.Exists(localFile))
                        {
                            LogSuccess($"Saved: {fileName}");
                            _pulled++;
                            if (deleteAfter)
                            {
                                await RunAdb($"shell rm '{remote}'");
                                LogInfo($"  Deleted from device: {fileName}");
                            }
                        }
                        else LogError($"Failed to pull: {fileName}");
                    }

                    done++;
                    SetProgress(done, _totalFiles);
                    UpdateStats();
                }

                LogSuccess($"Done! {_pulled} new, {_skipped} already downloaded.");
                if (_pulled > 0) OpenDestFolder();
            }
            catch (Exception ex) { LogError($"Error: {ex.Message}"); }
            finally
            {
                _running           = false;
                _btnPull.Text      = "⬇  Pull logs";
                _btnPull.BackColor = ColGreen;
            }
        }

        void CancelPull()
        {
            _cts?.Cancel();
            _btnPull.Text      = "⬇  Pull logs";
            _btnPull.BackColor = ColGreen;
        }

        // ── ADB auto-download ────────────────────────────────────────────────
        async Task DownloadAdb()
        {
            string url     = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip";
            string zipPath = Path.Combine(Path.GetTempPath(), "platform-tools.zip");
            string adbDir  = Path.Combine(AppContext.BaseDirectory, "adb");

            try
            {
                LogInfo("Downloading from Google…");
                SetDevice(false, "Downloading ADB…", "");

                using var wc = new WebClient();
                wc.DownloadProgressChanged += (s, e) =>
                    Invoke(() => SetDevice(false, $"Downloading ADB… {e.ProgressPercentage}%", ""));
                await wc.DownloadFileTaskAsync(url, zipPath);

                LogInfo("Extracting…");
                SetDevice(false, "Extracting…", "");
                await Task.Run(() => {
                    if (Directory.Exists(Path.Combine(Path.GetTempPath(), "platform-tools")))
                        Directory.Delete(Path.Combine(Path.GetTempPath(), "platform-tools"), true);
                    ZipFile.ExtractToDirectory(zipPath, Path.GetTempPath());
                });

                string src = Path.Combine(Path.GetTempPath(), "platform-tools");
                if (!Directory.Exists(adbDir)) Directory.CreateDirectory(adbDir);
                foreach (var f in Directory.GetFiles(src))
                    File.Copy(f, Path.Combine(adbDir, Path.GetFileName(f)), true);

                try { File.Delete(zipPath); } catch { }

                _adbPath = Path.Combine(adbDir, "adb.exe");
                LogSuccess("ADB ready.");
                RefreshDevice();
            }
            catch (Exception ex)
            {
                LogError($"ADB download failed: {ex.Message}");
                SetDevice(false, "ADB download failed", "");
            }
        }
    }
}
