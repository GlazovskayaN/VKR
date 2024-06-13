<?php
$menuJson = '[{"ID":"a_1","pict":"","active":true,"groups":[{"ID":"a_1697287665830","active":true,"sort":1,"title":"Таблица 1","type":"table","form_id":"1","rel_form_id":"2"}],"title":"Папка 1"}]';
$json_1 = '{"ID":1,"table_name":"ag_table_procedure","editable":"","table_name_1C":"","rel_form_id":2,"tabs":[{"ID":193,"sort":96830,"tab_name":"maintab","alignment":"horizontally","editable":"1","visible":"0","groups":[{"ID":"a_485","sort":96850,"title":"","alignment":"horizontally","editable":"0","type":"table","visible":1,"TABLE":"ag_table_procedure","groups":[{"ID":"id","sort":1,"name_ru":"ID","type":"number","visible":1,"name":"id","required":0,"field_1c":"id","rel_id":0,"TABLE":""},{"ID":"proc_code","sort":2,"name_ru":"Код","type":"text","visible":1,"name":"proc_code","required":0,"field_1c":"proc_code","rel_id":0,"TABLE":""},{"ID":"proc_name","sort":2,"name_ru":"Название","type":"text","visible":1,"name":"proc_name","required":0,"field_1c":"proc_name","rel_id":0,"TABLE":""}]}]}]}';
$json_2 = '{"ID":2,"table_name":"ag_table_procedure","editable":"","table_name_1C":"Справочники.хс_КатегорииТоваров","rel_form_id":1,"tabs":[{"ID":192,"sort":96750,"tab_name":"maintab","alignment":"horizontally","editable":"1","visible":"0","groups":[{"ID":"a_483","sort":96770,"title":"","alignment":"vertically","editable":"0","visible":1,"TABLE":"","groups":[{"ID":"id","sort":96820,"name_ru":"ID","editable":"0","type":"number","checked":"0","placeholder":"","link":"","visible":1,"name":"id","required":0,"format":"integer","field_1c":"id","TABLE":""},{"ID":"proc_code","sort":96820,"name_ru":"Код процедуры","editable":"1","type":"text","checked":"0","placeholder":"","link":"","visible":1,"name":"proc_code","required":1,"format":"","field_1c":"proc_code","TABLE":""},{"ID":"proc_name","sort":96820,"name_ru":"Название процедуры","editable":"1","type":"text","checked":"0","placeholder":"","link":"","visible":1,"name":"proc_name","required":1,"format":"","field_1c":"proc_name","TABLE":""}]}]}]}';

$file_9 = $_SERVER['DOCUMENT_ROOT'].'/functions/188.json';

$F9 = fopen($file_9,'r');
$json_9 = fread($F9, filesize($file_9));
fclose($F9);

$table_name = [
	1 => "ag_table_procedure",
	2 => "ag_table_procedure"
];