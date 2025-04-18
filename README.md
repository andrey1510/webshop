__README__
==========

Данное приложение представляет собой онлайн-магазин с возможностями просмотра выбора и заказа товаров, просмотра 
статистики заказов, использованием баланса.

Использованный стек: Java SE 21, Spring Boot, WebFlux, r2dbc, Redis, REST, Gradle, Lombok, Thymeleaf, H2, JUnit 5, Mockito.

Установка и запуск:
-----------------------------------

Склонируйте репозиторий, после чего:

**Вариант 1: установка в Docker-контейнер**

Должен быть установлен Docker. Запустите файл **docker-compose.yml** в IDE, или в директории ```webshop``` 
склонированного репозитория откройте командную строку и выполните команды:

```docker compose build```

```docker compose up -d```

В результате будет создан 3 контейнера - сервис магазина, сервис платежей, сервер Redis.

**Вариант 2: Запуск в IDE**

Должен быть установлен Docker. Запустите контейнер  **redis** в **docker-compose.yml** в IDE. Затем запустите
сервис магазина и сервис платежей.

Функционал приложения:
------------------------------------------

**Добавление товаров в магазин**

На странице ```http://localhost:8888/product-creator``` содержится форма для добавления продуктов в магазин. В форме 
нужно заполнить поля "Название товара", "Описание товара", "Цена товара", и, опционально, загрузить файл с изображением.
Изображение может быть формата ```.jpeg```, ```.jpg```, ```.png``` и размером не более 3 мегабайт.

После заполнения всей нужной информации, товар следует сохранить, нажав на кнопку ```Создать товар```. 
После успешного сохранения появится сообщение ```Товар успешно создан!``` (оно убирается нажатием крестика справа).

На витрину магазина с товарами можно перейти, нажав на кнопку "Перейти в магазин" справа сверху.

**Витрина магазина с товарами**

Витрина магазина со всеми товарами находится на странице ```http://localhost:8888/products```. У каждого товара 
имеется название, цена и изображение (опционально).

Товары можно фильтровать по названию (т.е. по вхождению комбинации символов в фильтре в название) или по цене 
(т.е. одномоментно либо по названию, либо по цене), и сортировать по алфавиту или цене (можно одновременно 
с фильтрацией). 

Товар можно добавить в корзину кнопкой ```Добавить в корзину```, затем изменить количество кнопками ```+``` и ```-```, 
а также удалить из корзины кнопкой ```Удалить```.

Кнопки управления пагинацией находятся внизу страницы.

Можно перейти в корзину и на страницу оформленных товаров с помощью кнопок справа сверху.

На страницу конкретного товара можно перейти, нажав на его название.

**Страница товара**

На странице товара ```http://localhost:8888/products/{id}``` имеется его название, описание, цена и 
изображение (опционально). Товар можно добавить в корзину кнопкой ```Добавить в корзину```, затем изменить количество 
кнопками ```+``` и ```-```, а также удалить из корзины кнопкой ```Удалить```.

**Баланс**

На данный момент в сервисе платежей фиксированный баланс в размере 1000 р.

**Корзина**

В корзине (```http://localhost:8888/cart```) находятся положенные в нее товары. В них имеется название, цена, 
количество, сумма всех экземпляров товара, а также сумма всего заказа в корзине. Также можно изменить количество
товара кнопками ```+``` и ```-```, и удалить товар из корзины кнопкой ```Удалить```.

Что оформить заказ, нужно нажать на кнопку ```Оформить заказ``` снизу. Тогда заказ в корзине перейдет в статус 
оформленного, а также произойдет перенаправление на страницу этого оформленного заказа. Нельзя оформить заказ, если 
корзина пустая (будет выдано соответствующее сообщение).

При оформлении заказа осуществляется автоматическая проверка баланса. Если сумма заказа в корзине превышает сумму 
баланса, то успешное оформление заказа будет невозможно. Кнопка ```Оформить заказ``` будет неактивна и будет выдано 
сообщение о недостаточном балансе.

После успешного оформления заказа его сумма будет вычтена из баланса.  

Можно перейти на витрину и на страницу оформленных товаров с помощью кнопок справа сверху. 

**Страница оформленного заказа**

На странице оформленного заказа ```http://localhost:8888/orders/{id}``` имеется список товаров в нем (у каждого 
название, изображение (опционально), цена, количество, общая цена экземпляров товара); общая сумма заказа. Также 
имеется номер, и дата и время оформления заказа.

Можно перейти на витрину, в корзину и на страницу оформленных товаров с помощью кнопок справа сверху.

**Страница всех оформленных заказов**

На странице всех оформленных заказов ```http://localhost:8888/orders``` имеется перечень всех оформленных заказов. 
У каждого есть номер, дата и время оформления, и сумма заказа. Также имеется общая сумма всех заказов.

На страницу конкретного оформленного заказа можно перейти, нажав на его название.

Можно перейти на витрину и в корзину с помощью кнопок справа сверху.

Тесты
------------------------------------------

К приложению прилагаются интеграционные и юнит тесты.