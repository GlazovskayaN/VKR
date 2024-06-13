$(document).ready(function() {
    let url_to_api = location.origin + '/api/ag_extractor.php';
    function initButtonView(){
        $('.ui .button__view').on('click', function(){
            let val1 = $(".select__protocol").val();
            let val2 = $(".select__apteka").val();
            if(val1)
                reqToDB(val1, val2);
        })
    }
    async function reqToDB(protocol, apteka){
        let type = 1;
        let range = 6;
        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({actions:'GetMapColors', params: {in_protocol: protocol, in_apt: apteka, in_type: type, in_div: range}}),
            success: function(response)
            {
                try{
                    let data = JSON.parse(response);
                    recolorMap(data);
                }catch(e){
                    console.log(e);
                }
            }
        })
    }
    function recolorMap(data){
        let paths = $(".russia-svg svg path");
        paths.each(function(){
            if(data[this.id]){
                $(this).attr('fill', data[this.id]);
            }else{
                $(this).attr('fill', 'rgba(0,0,0,0.2)');
            }
        })
    }
    async function init(){
        let options = "<option value='------' disabled selected>Выберите болезнь</option>"
        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({TableID: 'med.ag_table_disease_exist_v', actions:'GetTableInDB', params: {headers: 'id,dis_name'}}),
            success: function(response)
            {
                try{
                    let data = JSON.parse(response);
                    data.sort((a,b) =>{
                        const nameA = a.dis_name.toUpperCase();
                        const nameB = b.dis_name.toUpperCase();
                        if (nameA < nameB) {
                            return -1;
                        }
                        if (nameA > nameB) {
                            return 1;
                        }

                          // names must be equal
                        return 0;
                    })
                    data.forEach((row)=>{
                        options+="<option value='" + row['id'] + "'>" + row['dis_name'] + "</option>";
                    })
                }catch(e){
                    console.log(e);
                }
                $('.select__protocol').html(options);
                initButtonView();
            }
        })

        let options2 = "<option value='null' selected>Выберите аптеку</option>"
        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({TableID: 'med.ag_table_apteka', actions:'GetTableInDB', params: {headers: 'id,apt_name'}}),
            success: function(response)
            {
                try{
                    let data = JSON.parse(response);
                    data.sort((a,b) =>{
                        const nameA = a.apt_name.toUpperCase();
                        const nameB = b.apt_name.toUpperCase();
                        if (nameA < nameB) {
                            return -1;
                        }
                        if (nameA > nameB) {
                            return 1;
                        }

                          // names must be equal
                        return 0;
                    })
                    data.forEach((row)=>{
                        options2+="<option value='" + row['id'] + "'>" + row['apt_name'] + "</option>";
                    })
                }catch(e){
                    console.log(e);
                }
                $('.select__apteka').html(options2);
                initButtonView();
            }
        })
    }
    
    init();
});