#### Upgrading to ${TB_EDGE_VERSION}EDGE

**WinstarCloud Edge package download:**
```bash
wget https://github.com/winstarcloud/winstarcloud-edge/releases/download/v${TB_EDGE_TAG}/tb-edge-${TB_EDGE_TAG}.rpm
{:copy-code}
```
##### WinstarCloud Edge service upgrade

Install package:
```bash
sudo rpm -Uvh tb-edge-${TB_EDGE_TAG}.rpm
{:copy-code}
```
${UPGRADE_DB}
