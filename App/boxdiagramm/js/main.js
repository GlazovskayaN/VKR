$(document).ready(function() {

    let url_to_api = location.origin + '/api/ag_extractor.php';
    function initButtonView(){
        $('.ui .button__view').on('click', function(){
            let grug_id = $(".select__drugs").val();
            let reg_id = $('.select__regions').val();
            if(grug_id && reg_id)
                updateDiagramm(grug_id, reg_id);
        })
    }

    async function init(){
        let drug_options = "<option value='------' disabled selected>Выберите лекарство</option>";
        let reg_options = "<option value='null' selected>Выберите регион</option>";
        await $.ajax({
            type: "POST",
            url: url_to_api,
            data: JSON.stringify({TableID: 'med.ag_table_drug_exist_v', actions:'GetTableInDB', params: {headers: 'id,drug_name'}}),
            success: function(response)
            {
                try{
                    let data = JSON.parse(response);
                    data.sort((a,b) =>{
                        const nameA = a.drug_name.toUpperCase();
                        const nameB = b.drug_name.toUpperCase();
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
                        drug_options+="<option value='" + row['id'] + "'>" + row['drug_name'] + "</option>";
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
            data: JSON.stringify({ actions:'GetRegDrag', params: {drug_id: grug_id, reg_id: reg_id}}),
            // data: JSON.stringify({ actions:'GetRegDrag', params: {drug_id: 878, reg_id: 15}}),
            success: function(response)
            {
                console.log(response);
                let array = [];
                try{
                    let data = JSON.parse(response);
                    data.forEach((row)=>{
                        array.push(row['price']);
                    })
                    var plot_data = [
                        {
                            x: array.sort((a, b) => a - b),
                            type: 'box',
                            orientation: 'h'
                        }
                    ];
                    
                    var layout = {
                        title: 'Средняя стоимость',
                        xaxis: {
                            title: ''
                        },
                        yaxis: {
                            title: ''
                        },
                        showlegend: false
                    };

                    Plotly.newPlot('boxplot', plot_data, layout);
                }catch(e){
                    console.log(e);
                }
            }
        })
    }

    init();
});