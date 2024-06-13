<?php
function OpenPSGConnection(){
    $host= '192.168.110.26';
    $db = 'ag_med';
    $port  = 5432;
    $user = 'sa';
    $password = 'aag20240319';
    try {
        $dsn = "pgsql:host=$host;port=$port;dbname=$db;";
        $pdo = new PDO($dsn, $user, $password, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);
        return $pdo;
    } catch (PDOException $e) {
        die($e->getMessage());
    } finally {
        $pdo = null;
    }
    return null;
}

function pg_query_simple($q){
    $con = OpenPSGConnection();
    return $con->query($q)->fetchAll();
}
