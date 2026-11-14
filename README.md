# Yerel hastane randevu uygulaması

Spring Boot, Java 17 ve gömülü H2 kullanır. Projeyi Windows üzerinde başlatmak için hazır JAR bulunan klasör yapısında Baslat.cmd dosyasını çalıştırın. Tarayıcıda http://localhost:8080 adresini açın. Sunucu yalnızca bu bilgisayardan erişilebilen 127.0.0.1 adresini dinler.

Kaynak koddan çalıştırmak için JDK 17 ve Maven 3.9 ile proje kökünde:

```shell
mvn clean package
java -jar target/hastane-1.0.0.jar
```

İlk çalıştırmada yönetici hesabı oluşturun. Önce şube, bölüm, hizmet, çalışan, hizmet ataması ve mesai aralığı tanımlayın. Hasta hesabı açıp uygun bir saat seçin. Çalışan hesabıyla giriş yapıp talebi değerlendirin. Aynı anda farklı hesaplar için ayrı tarayıcı profilleri kullanabilirsiniz.

Veri proje kökünde veri/hastane.mv.db dosyasına kaydedilir. Uygulamayı çalıştıran terminalde Ctrl+C ile durdurabilirsiniz. Bir sonraki açılışta aynı klasörden başlatın.
