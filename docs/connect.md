# Pixel Watch 4 — ADB Wireless Debugging

## Voraussetzungen

- Pixel Watch 4 und Mac im **selben WLAN-Netzwerk**
- Auf der Uhr: **Settings → Developer options → Wireless debugging: AN**
- Mac-Firewall deaktiviert oder ADB gewhitelistet

## Verbindung herstellen

### 1. ADB-Server mit korrektem mDNS-Backend starten

```bash
export ADB_MDNS_OPENSCREEN=0
adb kill-server
adb start-server
```

> **Wichtig:** Ohne `ADB_MDNS_OPENSCREEN=0` findet mDNS die Uhr nicht zuverlässig auf macOS.

### 2. Uhr per mDNS finden (optional)

```bash
adb mdns services
```

Zeigt die Uhr mit IP, Pairing-Port und Connect-Port an.

### 3. Pairing durchführen

Auf der Uhr: **Wireless debugging → Pair new device** — PIN und Port notieren.

```bash
adb pair <IP>:<PAIRING_PORT> <PIN>
```

Erwartete Ausgabe:
```
Successfully paired to <IP>:<PORT> [guid=adb-5B211WRBNL302V-rrn4CY]
```

> Das Pairing bleibt bestehen, bis `adb kill-server` ausgeführt wird oder Wireless Debugging auf der Uhr zurückgesetzt wird.

### 4. Verbinden

Den **Connect-Port** verwenden (nicht den Pairing-Port!). Steht unter Wireless Debugging auf der Uhr oder in der `adb mdns services` Ausgabe.

```bash
adb connect <IP>:<CONNECT_PORT>
```

### 5. Prüfen

```bash
adb devices
```

Die Uhr erscheint als `<IP>:<PORT> device`.

## Bekannte Probleme

| Problem | Ursache | Lösung |
|---------|---------|--------|
| `error: protocol fault (couldn't read status message): Undefined error: 0` | mDNS-Backend Openscreen funktioniert nicht zuverlässig auf macOS | `export ADB_MDNS_OPENSCREEN=0` vor `adb start-server` setzen |
| `No route to host` | Uhr und Mac in unterschiedlichen Netzwerken oder WLAN-Verbindung der Uhr unterbrochen | Gleiches WLAN sicherstellen, IP der Uhr prüfen |
| `failed to connect` nach erfolgreichem Pairing | Falscher Port (Pairing-Port statt Connect-Port) | Connect-Port aus Wireless Debugging oder `adb mdns services` verwenden |
| Uhr verschwindet nach `adb kill-server` | Pairing-Credentials werden gelöscht | Erneut pairen (Schritt 3) |

## Schnellstart (nach erstem Pairing)

```bash
export ADB_MDNS_OPENSCREEN=0
adb kill-server
adb start-server
# Auf der Uhr: Wireless debugging → Pair new device
adb pair <IP>:<PAIRING_PORT> <PIN>
adb connect <IP>:<CONNECT_PORT>
```
