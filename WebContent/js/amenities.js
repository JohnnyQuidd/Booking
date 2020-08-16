$(document).ready(() => {
	fetchAmenities();
    
    $('#addAmenity').click(() => {
        let amenityName = $('#amenityName').val();
        let id = 1;
        $.post({
            url: '../rest/amenity',
                data: JSON.stringify({id, amenityName}),
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    window.location.href = 'amenities.html';
                },
                error: err => {
                    alert(err.responseText);
                }
        });
    });

});

function fetchAmenities() {
    $.get({
        url: '../rest/amenity',
        dataType: 'json',
        success: amenities => {
            appendTable(amenities);
        },
        error: err => {
            alert(err.responseText);
        }
    });
}

function appendTable(amenities) {
    let i;
    for (i=0; i < amenities.length; i++) {
        $('#amenitiesTable tr:last').after(`
        <tr>
            <th scope="row">` + (i+1) + `</th>
            <td> ` + amenities[i].amenity + `</td>
            <td>`+ (amenities[i].active ? `Yes` : `No`) + `</td>
            <td>`+ `<button class="btn btn-primary"> Edit</button> <button class="btn btn-danger"> Delete</button>` + `</td>
        </tr>`);
    }
}