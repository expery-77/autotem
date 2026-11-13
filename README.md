# Auto Totem (Fabric 1.21.4)

Offhand'de totem yoksa envanterden otomatik totem koyar.

## Kullanım
- **R**: AutoTotem aç/kapat
- **Sağ Shift**: Ayar menüsünü aç/kapat (aynı tuşa tekrar basınca kapanır)
- Tuşlar Ayarlar > Kontroller > AutoTotem altından değiştirilebilir
- HUD: ekranda `AutoTotem ON/OFF  xN` (N = envanterdeki totem sayısı)
- Ayarlar `config/autototem.json` dosyasına kaydedilir.

## Jar'ı Android'de derleme (GitHub ile, bilgisayar gerekmez)
1. Zip'i telefonda aç (klasöre çıkar).
2. github.com > yeni repo oluştur (Chrome'da "Masaüstü sitesi" aç).
3. Add file > Upload files > çıkardığın TÜM dosyaları seç (zip'in kendisi hariç) > Commit.
4. Repoda `build.yml` dosyasını aç > kalem (düzenle) > dosya adı kutusunu
   `.github/workflows/build.yml` yap > Commit.
5. Actions sekmesi > en son "build" çalışması yeşil olunca en alttaki `autototem-jar` dosyasını indir.
6. İndirdiğin zip'in içinden `autototem-1.0.0.jar` dosyasını çıkar.

## Oyunda
`mods` klasörüne at: bu jar + **Fabric API 0.119.4+1.21.4** (Modrinth'ten).
Fabric Loader 0.16.10+ gerekir. PojavLauncher'da Fabric profili ile çalışır.
