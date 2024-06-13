<?php
session_start();
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Credentials: true');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: X-Requested-With, origin, content-type, accept');
require_once($_SERVER["DOCUMENT_ROOT"]."/add_infoblock/pgs_connect.php");
require_once($_SERVER["DOCUMENT_ROOT"]."/functions/init.php");

$arrPost = json_decode(file_get_contents("php://input"),true);

if ($arrPost['actions'] == "GetMenu"){
    $login = $_SESSION['login'];
    $q = "select a.j_table, a.read_only from med_set.ag_roles a, med.ag_users b where a.role_name = b.ag_role_name and b.login_xx = '".$login."' limit 1";
    $r = pg_query_simple($q);
    echo json_encode($r[0],JSON_INVALID_UTF8_IGNORE);

}elseif ($arrPost['actions'] == "GetUserRole"){
    echo "ALL";
}elseif ($arrPost['actions'] == "GetUserName"){
    echo setSessionToJS(1);
}elseif ($arrPost['actions'] == "GetStructureDocument"){
    $q = "SELECT * FROM med_set.ag_set WHERE id=".$arrPost['TableID'];
    $r = pg_query_simple($q);

    echo $r[0]['j_table'];
}elseif ($arrPost['actions'] == "GetMapColors"){
    if(!isset($arrPost['params']['in_div']) || $arrPost['params']['in_div'] == ""){
        $arrPost['params']['in_div']=6;
    }
    $q = "select * from med.get_map_price_color(".$arrPost['params']['in_protocol'].",".$arrPost['params']['in_apt'].",".$arrPost['params']['in_type'].",".$arrPost['params']['in_div'].")";
    $r = pg_query_simple($q);
    $res = [];
    foreach ($r as $val){
        $res[$val["code"]]=$val["color"];
    }
    echo json_encode($res,JSON_INVALID_UTF8_IGNORE);
}
elseif ($arrPost['actions'] == "GetValuesListForm"){
    echo extractNewDateList($arrPost);
}elseif ($arrPost['actions'] == "GetRegDrag"){
    $q = "select * from med.get_drug_reg_avg_price(".$arrPost['params']['reg_id'].",".$arrPost['params']['drug_id'].")";
    $r = pg_query_simple($q);
    echo json_encode($r,JSON_INVALID_UTF8_IGNORE);
}elseif ($arrPost['actions'] == "GetRoundRegDrag"){
    $q = "select * from med.get_manufacturer_country(".$arrPost['params']['drug_id'].",".$arrPost['params']['reg_id'].")";
    $r = pg_query_simple($q);
    echo json_encode($r,JSON_INVALID_UTF8_IGNORE);
}elseif ($arrPost['actions'] == "CountElementsInTable"){
    echo countRows($arrPost);
}elseif ($arrPost['actions'] == "GetValuesDocument"){
    echo extractNewData($arrPost);
}elseif ($arrPost['actions'] == "update"){
    $q = "select med.ag_upg_f('".json_encode($arrPost,JSON_INVALID_UTF8_IGNORE)."')";
    $r = pg_query_simple($q);
    echo json_encode($r,JSON_INVALID_UTF8_SUBSTITUTE);
}elseif ($arrPost['actions'] == "insert"){
    $q = "select med.ag_upg_f('".json_encode($arrPost,JSON_INVALID_UTF8_IGNORE)."')";

    $r = pg_query_simple($q);
    echo json_encode($r,JSON_INVALID_UTF8_SUBSTITUTE);
}elseif ($arrPost['actions'] == "delete"){
    $q = "select med.ag_upg_f('".json_encode($arrPost,JSON_INVALID_UTF8_IGNORE)."')";
    $r = pg_query_simple($q);
    echo json_encode($r,JSON_INVALID_UTF8_SUBSTITUTE);
}elseif ($arrPost['actions'] == "GetValuesTable"){
    echo extractValuesTable($arrPost);
}elseif ($arrPost['actions'] == "GetList"){
    echo getList($arrPost);
}elseif ($arrPost['actions'] == "GetTableInDB"){
    echo getTableFromDB($arrPost);
}elseif ($arrPost['actions'] == "GetPhotoFromSIOPR" && $arrPost['TableID'] == 32){
    echo getPhotoFromSIOPR($arrPost);
}
