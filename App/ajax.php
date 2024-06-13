<?
require_once($_SERVER["DOCUMENT_ROOT"]."/functions/init.php");
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Credentials: true');
header('Access-Control-Allow-Methods: GET,POST');
header('Access-Control-Allow-Headers: X-Requested-With, origin, content-type, accept');
session_start();
$arrPost = json_decode(file_get_contents("php://input"), true);
$table = "med.ag_users";

if($_POST['login'] != '' && $_POST['password'] != ''){

    $log = $_POST['login'];
    $pass = $_POST['password'];
    doHash($pass, $log);
}else if($arrPost['login'] != '' && $arrPost['pass'] != ''){
    $log = $arrPost['login'];
    $pass = $arrPost['pass'];
    doHash($pass, $log);
}else{
    echo "Забыли что то ввести";
}

function doHash($p, $u){

    global $table;
    function verified($p, $u){
        if($p == 1){
            /* ABUSER */
            $_SESSION['login'] = $u;
            $_SESSION['pass'] = $_POST['password'];
            $_SESSION['verified'] = 1;
        }else{
            $_SESSION['login'] = $u;
            $_SESSION['verified'] = 0;
        }
    }

    /* подключаем БД */

    /* выбираем хеш по логину */

    $q = "SELECT hash_xx FROM ".$table." WHERE login_xx='".$u."'";
//    echo $q;
    $result = pg_query_simple($q);
    if(count($result)>0){
        /* если пользователь есть то вот его хеш */

        $hash = $result[0]['hash_xx'];
        $res = sha1($p, false);
        $res = hex2bin($res);
        $res = base64_encode($res);
        if($res == explode("," , $hash)[0]){
            //UpdateUserFrom1C();
            echo "GO";
            verified(1, $u);
        }else{
            echo "STOP";
        }
    }else{
        verified(0, $u);
        echo "Нет такого пользователя";
    }
}
function simpleLog($u, $status){
    $dF = $_SERVER['DOCUMENT_ROOT']."/log/".date("Y-m-d").".txt";
    $str = date("H:i:s")." || ".$status." || ".$u."\r";
    if(file_exists($dF)){
        $type = "FILE_APPEND";
    }else{
        $type = "";
    }
    file_put_contents($dF,$str,$type);
}