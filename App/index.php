<html>
<head>

    <title>Курсовая</title>
    <script src="./js/jquery-3.6.0.js"></script>
    <link rel="stylesheet" href="./css/bootstrap.min.css">
    <link rel="stylesheet" href="./css/bootstrap-icons.css">
    <link rel="stylesheet" href="./css/style.css">
    <link rel="stylesheet" href="./css/index_style.css">
    <link rel="stylesheet" href="./css/chosen.min.css">
    <script src="./js/scripts.js" ></script>
    <script src="./js/index_script.js" ></script>
    <script src="./js/chosen.jquery.min.js" ></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="#FFFFFF">
<script src="./js/bootstrap.bundle.min.js"></script>

<div class="login">
    <div class="login__main">
        <div class="login__container">
            <header class="login__header">
            </header>

            <main>
                <form id="login-form" class="login__content" accept-charset="utf-8">

                    <p class="login__content-p">Добро пожаловать, рады снова видеть Вас.</p>

                    <input type="text" class="input-normal" id="login" placeholder="Логин"/>

                    <input type="password" class="input-normal" id="pass" placeholder="Введите пароль"/>

                    <p class="login__error" style="display: none">Неверный логин или пароль</p>

                    <div class="login__content-checkbox">
                        <!--<label class="container">Чужой компьютер
                            <input type="checkbox" checked="checked">

                            <span class="checkmark"></span>
                        </label>-->

                        <a class="container remember-pass" href="mailto:siopr@mos.ru?subject=Проблема со вводом пароля"
                           title="По вопросу восстановления пароля напишите в техподдержку: siopr@mos.ru">

                        </a>
                    </div>

                    <input type="submit" class="login__btn-submit js-btn-submit" value="Войти">
                </form>
            </main>

            <footer class="login__footer">

                <p style="font-weight: bold">Дипломная работа 2024</span></p>
            </footer>
        </div>
    </div>
    <div class="login__image">
        <img class="login__background" alt="Логинение" />
    </div>
</div>

<?php
//    require_once('html-scripts.php');
?>
