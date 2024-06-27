# raifTest
Тестовое задание для raifaizen

## Запуск приложения
    
### Что потребуется для запуска
    1) Maven
    2) docker
    3) java 17 или выше

### Настройки и запуск
Для запуска приложения необходимо добавить два файла "application.properties" и "application-test.properties"
в папку resources. Первый файл нужен для запуска приложения, второй для тестирования(тестирования репозиториев).
Наполнение файлов должно быть такими:

```
    spring.application.name=raifTest

spring.datasource.driver-class-name =org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5433/postgres?currentSchema=raif
spring.datasource.username={логин для бд}
spring.datasource.password={Пароль для бд}
#если не менять настройки docker-compose, то логин: postgres, пароль: password
spring.datasource.isolationLevel=TRANSACTION_REPEATABLE_READ

flyway.baseline-on-migrate=true

hikari.cachePrepStmts=true
hikari.prepStmtCacheSize=250
hikari.prepStmtCacheSqlLimit=2048

#Echo all executed sql to console
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=true 
hibernate.format_sql=true
hibernate.highlight_sql=true
logging.level.org.hibernate.SQL=debug

#Automatically export the scheme
hibernate.hbm2ddl.auto=none

#Token secrets
jwt.secret={секретный ключ для токена}
refresh.secret={другой секретный ключ для токена}
```

Далее перед запуском приложения необходимо запустить докер-контейнер при помощи команды "docker-compose up"

После того как контейнер запустится можно запускать spring-boot приложение

## Отправляемые модели
### CustomerDto
+ id - id клиента - UUID
+ name - имя клиента - String
+ username - username клиента(Уникальный) - String
+ password - пароль - String

### AccountDto
+ serialNumber - Номер счета - String
+ customerId - id клинта, которому принадлежит счет - UUID
+ balance - баланс счета - double
+ creationDate - дата создания
+ type - тип валюты - AccountType

### AccountCreationRequestModel
Поля:
+ currency - Варианты валют в которой можно открыть счет - AccountType(USD, RUB, EUR).
    

## Эндпоинты
    
    Все эндпоинты в приложении требуют авторизации, кроме следующих
    
| Путь                 | тип запроса | что принимает      | что делает                    | что вернет  |
|----------------------|-------------|--------------------|-------------------------------|-------------|
| /customers           | Post        | CustomerDto        | регистрация нового клиента    | accessToken |
| /customers           | Put         | username, password | вход в аккаунт                | accessToken |
| /customers/refreshes | Put         | -                  | генерирует новую пару токенов | accessToken | 


    Эндпоинты тербующие авторизации. Информацию по тому для какого клиента надо выдать информациию или 
    выполнить действия берется из jwt 

| Путь                | тип запроса | что принимает                                    | что делает                                                            | что вернет       |
|---------------------|-------------|--------------------------------------------------|-----------------------------------------------------------------------|------------------|
| /customers/delete   | Delete      | -                                                | Удаление клиента, если нет счетов                                     | boolean          |
| /customers          | get         | -                                                | Получение текущего пользователя                                       | AccountDto       |
| /accounts/{serial}  | Get         | String serial (Path var)                         | получение клиента по номеру счета(serial)                             | AccountDto       |
| /accounts/          | Get         | -                                                | Получение всех счетов по клиенту                                      | List<AccountDto> |
| /accounts/dates     | Get         | Date date (query)                                | Получение счетов клиента начиная с выбранной даты создания            | List<AccountDto> |
| /accounts/balance   | Get         | double balance (query)                           | Получение счетов клиента по балансу                                   | List<AccountDto> | 
| /accounts/types     | Get         | AccountType type (query)                         | Получение счетов по типу хранимой валюты                              | List<AccountDto> |
| /accounts/transfer  | Put         | String serialFrom String serialTo, double amount | Перевод со счета на счет(нельзя переводить меньше, чем 0.1 ед валюты) | Boolean          |
| /accounts/withdraws | Put         | String serial, double amount                     | Вывод со счета(нельзя выводить меньше, чем 0.1 ед валюты)             | Boolean          |
| /accounts/refils    | Put         | String serial, double amount                     | Пополнение счета(нельзя снимать меньше, чем 0.1 ед валюты)            | Boolean          |
| /accounts/          | Post        | AccountCreationRequestModel creationRequest      | Создание счета с выбранным типом счета                                | Boolean          |
| /accounts/{serial}  | Delete      | String serial                                    | Удаление счета клиента                                                | Boolean          |


## Коды ошибок по эндпоинтам
    Везде может выпасть ошибка 403Forbiden, значит надо запростить новую пару токенов или 
    попытка получить счет, который не отноститься к залогинившемуся клиенту

    Если по одному эндпоинту два и больше одинаковых кода ошибки, то причина пишется в header ответа

+ /accounts/{serial}
    - 404 not Found - не найден такой счет
+ /accounts/transfer
    - 400 bad Request - Одинаковые номера счетов, не хватает денег для перевода или перевод невозможно осуществить
    - 404 Not Found - нет либо счета с которого отправляют, либо на который переводят
    - 507 Insufficient Storage - Проблема при записи перевода в бд
+ /accounts/withdraws
    - 400 bad Request - Не хватает денег для перевода или перевод невозможно осуществить
    - 404 Not Found - нет счета, с которого пытаются вывести деньги
    - 507 Insufficient Storage - Проблема при записи перевода в бд
+ /accounts/refils
    - 400 bad Request - Перевод невозможно осуществить
    - 404 Not Found - нет счета, с которого пытаются вывести деньги
    - 507 Insufficient Storage - Проблема при записи перевода в бд
+ /accounts (Post)
    - 507 Insufficient Storage - Проблема при записи модели в бд
+ /accounts/{serial} (Delete)
    - 400 bad Request - Если при удалении счета баланс не пустой
    - 404 Not Found - Если такого счета нет
    - 507 Insufficient Storage - Проблема при удалении из бд
+ /customers (Put)
    - 400 badRequest - Если нет такого клиента или не правильный пароль
+ /customers (Post) 
    - 400 bad Request - Если есть такой username в бд
    - 507 Insufficient Storage - Проблема при записи модели в бд
+ /customers/delete
    - 400 badRequest - если у клиента есть не закрытые счета
    - 404 NotFound - если не получилось найти клиента
    - 507 Insufficient Storage - Проблема при удалении из бд