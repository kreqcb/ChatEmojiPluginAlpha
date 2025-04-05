ChatEmojiPlugin для Minecraft 1.20.1

Этот плагин добавляет кастомные эмодзи в чат. Пишешь :D_jokerge: — и в чате появляется картинка Pepe. Всего 12 эмодзи, они уже настроены.

Плагин работает на Spigot 1.20.1 (и на Paper 1.20.1 тоже). Если нужна другая версия, можно пересобрать (смотри ниже).

Что есть в проекте:
- pom.xml — файл с зависимостями (настроен для Spigot 1.20.1).
- src/main/java/com/yourname/ChatEmojiPlugin.java — основной код плагина.
- src/main/resources/plugin.yml — описание плагина.

Как собрать и установить:
1. Убедись, что у тебя есть Java 17 и Maven:
   - Проверь Java: java -version (должно быть 17).
   - Если нет, скачай Java 17: https://adoptium.net/
   - Maven скачай тут: https://maven.apache.org/download.cgi (и добавь в PATH).
2. Открой терминал, перейди в папку проекта:
   cd путь_к_папке_ChatEmojiPlugin
3. Собери плагин:
   mvn clean package
   - Файл ChatEmojiPlugin-1.0-SNAPSHOT.jar появится в папке target/.
4. Положи ChatEmojiPlugin-1.0-SNAPSHOT.jar в папку plugins твоего сервера.
5. Добавь папку images с картинками (16x16) и config.yml в plugins/ChatEmojiPlugin.
   - Пример config.yml и картинки можно взять из архива ChatEmojiPlugin_for_friend.zip.
6. Настрой Dropbox токен:
   - Зайди на https://www.dropbox.com/developers/apps, создай приложение.
   - Включи права: files.content.write и sharing.write.
   - Сгенерируй токен в "Generated access token".
   - Вставь токен в ChatEmojiPlugin.java (найди строку String accessToken = "твой_токен";).
   - Пересобери плагин (mvn clean package).
7. Запусти сервер (java -jar spigot-1.20.1.jar) и проверь эмодзи:
   :D_jokerge: :A_smeyalsya: :B_fear: :B_boyni: :A_glypi: :B_temno: :AB_cherh: :C_stare: :A_kruti: :D_saj: :D_smoke:

Как поменять версию Minecraft:
1. Открой pom.xml.
2. Найди зависимость:
   <dependency>
       <groupId>org.spigotmc</groupId>
       <artifactId>spigot-api</artifactId>
       <version>1.20.1-R0.1-SNAPSHOT</version>
       <scope>provided</scope>
   </dependency>
3. Поменяй версию, например:
   - Для 1.20.2: <version>1.20.2-R0.1-SNAPSHOT</version>
   - Для 1.20.4: <version>1.20.4-R0.1-SNAPSHOT</version>
4. Пересобери: mvn clean package

Если что-то не работает:
- Проверь, что у тебя Spigot или Paper 1.20.1.
- Убедись, что токен Dropbox вставлен.
- Скинь мне latest.log (из папки logs), я помогу.
