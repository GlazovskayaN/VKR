$(document).ready(function() {

    let url_to_api = location.origin + '/api/ag_extractor.php';
    function initButtonView(){
        $('.ui .button__view').on('click', function(){
            let grug_id = $(".select__drugs").val();
            let reg_id = $('.select__regions').val();
            if(grug_id)
                updateDiagramm(grug_id, reg_id);
        })
    }

    async function init(){
        let drug_options = "<option value='------' disabled selected>Выберите болезни</option>";
        let reg_options = "<option value='null' selected>Выберите регион</option>";
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
                        drug_options+="<option value='" + row['id'] + "'>" + row['dis_name'] + "</option>";
                    })
                }catch(e){
                    console.log(e);
                }
                $('.select__drugs').html(drug_options);
                
            }
        })

        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({TableID: 'med.ag_table_region', actions:'GetTableInDB', params: {headers: 'id,region'}}),
            success: function(response)
            {
                try{
                    let data = JSON.parse(response);
                    data.sort((a,b) =>{
                        const nameA = a.region.toUpperCase();
                        const nameB = b.region.toUpperCase();
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
                        reg_options+="<option value='" + row['id'] + "'>" + row['region'] + "</option>";
                    })
                }catch(e){
                    console.log(e);
                }
                $('.select__regions').html(reg_options);
                
            }
        })

        initButtonView();
    }

    async function updateDiagramm(grug_id, reg_id){
        
        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({ actions:'GetRoundRegDrag', params: {drug_id: grug_id, reg_id: reg_id}}),
            success: function(response)
            {

                console.log(response);
                let array = [];
                let labels = [];
                try{
                    let data = JSON.parse(response);
                    console.log(data);
                    data.forEach((row)=>{
                        array.push(row['num']);
                        labels.push(row['manufacturer'] + ": " + row['num'] + "%");
                    })
                    var plot_data = [
                        {
                            values: array,
                            labels: labels,
                            type: 'pie',
                            textinfo: 'none', // Не отображать текстовую информацию
                            hoverinfo: 'none',
                            insidetextorientation: 'none'
                        }
                    ];
                    
                    var layout = {
                        title: 'Производители',
                    };

                    Plotly.newPlot('piechart', plot_data, layout);
                }catch(e){
                    console.log(e);
                }
            }
        })
    }

    init();
});