#!/usr/bin/env bash
# Script de Otimização de Desempenho, Remoção de Anúncios/Telemetria e Instalação
# do Omarchy TV Launcher ("Osaka Jade") para Smart TVs Semp TCL (Android TV)
set -euo pipefail

TV_IP="${1:-}"
if [ -z "$TV_IP" ]; then
  echo "Uso: ./optimize-tv.sh <IP_DA_TV:5555>"
  exit 1
fi

echo "[1/4] Conectando na Smart TV via ADB ($TV_IP)..."
adb connect "$TV_IP"

echo "[2/4] Desativando 22 pacotes de telemetria, anúncios e bloatware da TCL/Google..."
adb -s "$TV_IP" shell '
for pkg in \
  com.tcl.bi \
  com.tcl.tvweishi \
  com.tcl.appmarket2 \
  com.tcl.usercenter \
  com.tcl.partnercustomizer.resource \
  com.tcl.useragreement \
  com.tcl.versionUpdateApp \
  com.tcl.copydatatotv \
  com.google.android.leanbacklauncher \
  com.google.android.leanbacklauncher.recommendations \
  com.google.android.leanbacklauncher.partnerwidget \
  com.google.android.leanbacklauncher.partnercustomizer \
  com.google.android.tungsten.setupwraith \
  com.google.android.tv.bugreportsender \
  com.google.android.music \
  com.google.android.syncadapters.contacts \
  com.google.android.syncadapters.calendar \
  com.android.providers.calendar \
  com.android.providers.contacts \
  com.android.providers.userdictionary \
  com.android.wallpaper.livepicker \
  com.android.dreams.basic; do
  echo -n "  - $pkg: "
  pm disable-user --user 0 "$pkg" 2>&1 || pm uninstall -k --user 0 "$pkg" 2>&1 || true
done
'

echo "[3/4] Otimizando memória RAM, processos em segundo plano, animações e storage TRIM..."
adb -s "$TV_IP" shell '
settings put global activity_manager_constants max_cached_processes=4,background_settle_time=10000
settings put global window_animation_scale 0
settings put global transition_animation_scale 0
settings put global animator_duration_scale 0
settings put global fstrim_mandatory_interval 86400000
settings put secure send_action_app_error 0
settings put global wifi_scan_always_enabled 0
settings put global ble_scan_always_enabled 0
for pkg in com.netflix.ninja com.google.android.youtube.tv com.amazon.amazonvideo.livingroom com.disney.disneyplus com.globo.globotv com.crunchyroll.crunchyroid; do
  cmd appops set "$pkg" RUN_IN_BACKGROUND ignore 2>/dev/null || true
  cmd appops set "$pkg" RUN_ANY_IN_BACKGROUND ignore 2>/dev/null || true
  am force-stop "$pkg" 2>/dev/null || true
done
am kill-all
sm fstrim 2>/dev/null || true
free -m
'

if [ -f "omarchy-tv-launcher.apk" ]; then
  echo "[4/4] Instalando Omarchy TV Launcher e definindo como tela inicial padrão..."
  adb -s "$TV_IP" install -r omarchy-tv-launcher.apk
  adb -s "$TV_IP" shell '
    cmd package set-home-activity org.omarchy.tvhome/.MainActivity 2>/dev/null || true
    am start -n org.omarchy.tvhome/.MainActivity
    input keyevent KEYCODE_HOME
  '
fi

echo "Concluído com sucesso!"
