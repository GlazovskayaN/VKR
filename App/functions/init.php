<?php
ini_set('display_errors', '0');
ini_set('display_startup_errors', '0');
error_reporting(0);

require_once($_SERVER["DOCUMENT_ROOT"]."/add_infoblock/pgs_connect.php");
require_once($_SERVER["DOCUMENT_ROOT"]."/functions/constants.php");


function getPhotoFromSIOPR($arr){

    $arr['TableID']="ag_table_protocol_dis_v";
    $res = json_decode(extractNewData($arr),true);
    $f_name = explode(".",$res[0]['file_name'])[0].".pdf";
    $JSON["__Naimenovanie"] = explode(".",$res[0]['file_name'])[0];
    $JSON["url"] = "http://lst10.camco/upload/".$f_name;
    $JSON["src"] = "http://lst10.camco/upload/".$f_name;
    $JSON["type"] = "pdf";
    $JSON["_Razmer"] = "345";
    echo json_encode([$JSON],JSON_INVALID_UTF8_IGNORE);

}

function getList($arr)
{

    $whr = $arr['params']['where'];
    if (strlen($whr) <= 0 || $whr == "a_4120859 = ''") {
        $whr = "1=1";
    }
    $px = substr($arr['TableID'], 0, 3);

    $limit = "ORDER BY 1 offset 0 ROWS FETCH NEXT 50 ROWS ONLY";

        $nam = "__Naimenovanie";

    if ($px == "_un") {
        $q = "SELECT *, (_Ssylka) as _Ssylka, " . $nam . " as value  FROM med." . $arr['TableID'] . " WHERE " . $whr . " " . $limit;
    } else {
        $q = "SELECT *, (_Ssylka) as _Ssylka, " . $nam . " as value  FROM med." . $arr['TableID'] . " WHERE " . $whr . " " . $limit;
    }


    $res = pg_query_simple($q);
    $str = json_encode($res, JSON_INVALID_UTF8_SUBSTITUTE);
    return $str;
}

function extractNewDateList($arr)
{
    GLOBAL $table_name;
    $pg = "";
    $where = "";
    $headers = "*";
    if(strlen($arr['params']['selectedPage']) > 0 && strlen($arr['params']['quantity']) > 0){
        $count = ((int)$arr['params']['selectedPage']-1) * (int)($arr['params']['quantity']);
        $pg = "offset ".$count." ROWS FETCH NEXT ".(int)$arr['params']['quantity']." ROWS ONLY";
    }
    if(strlen($arr['params']['where'])>0){
        $where = " WHERE ".$arr['params']['where'];
    }if(strlen($arr['params']['headers'])>0){
        $headers = $arr['params']['headers'];
    }
    $q="SELECT * FROM med.".$arr['TableID']. " ".$where." " . $pg;
    $res = pg_query_simple($q);
    return json_encode($res, JSON_INVALID_UTF8_SUBSTITUTE);
}

function extractNewData($arr)
{
    $q = "SELECT * FROM med.".$arr['TableID']." WHERE id = ".(int)$arr['params']['UID'];

    $res = pg_query_simple($q);

    return json_encode($res, JSON_INVALID_UTF8_SUBSTITUTE);
}


function extractValuesTable($arr)
{

    $whr = $arr['params']['where'];

    if (strlen($whr) <= 0) {
        $whr = "1=1";
    }
    $q = "select * from med.".$arr['params']['GroupID']." where ".$whr." and _ssylka = ".$arr['params']['UID'];
    $res = pg_query_simple($q);
    return json_encode($res, JSON_INVALID_UTF8_SUBSTITUTE);
}

function setSessionToJS($num)
{

    session_start();

    if($_SESSION['login'] == ""){
        header('Location: /', true, 301);
    }
    if($num == 0) {
        header('Content-type: application/json');
        return json_encode($_SESSION);
    }elseif ($num == 1){
        header('Content-type: application/json');
        $arr['userID'] = 0;
        $arr['name'] = $_SESSION['login'];
        return json_encode($arr);
    }

}

function getTableFromDB($arr)
{
    $where = "";
    $headers = "*";
    if (strlen($arr['params']['where']) > 0) {
        $where .= " WHERE " . $arr['params']['where'];
    }
    if (strlen($arr['params']['headers']) > 0) {
        $headers = $arr['params']['headers'];
    }
    if (strlen($arr['params']['order']) > 0) {
        $where .= " ORDER BY " . $arr['params']['order'];
    }
    $q = "SELECT " . $headers . " FROM " . $arr['TableID'] . $where;

    $res = pg_query_simple($q);
    $str = json_encode($res, JSON_INVALID_UTF8_SUBSTITUTE);
    return $str;
}

function countRows($arr)
{
    GLOBAL $table_name;

    $where = "";
    $headers = "*";
    if(strlen($arr['params']['where'])>0) {
        $where = " WHERE " . $arr['params']['where'];
    }
    $q="SELECT COUNT(*) FROM med.".$arr['TableID']. " ".$where;
    $res = pg_query_simple($q);
    return json_encode($res[0]['count'], JSON_INVALID_UTF8_SUBSTITUTE);

}
