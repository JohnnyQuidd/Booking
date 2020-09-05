$(document).ready(() => {
	fetchAmenities();
    
    $('#addAmenity').click(() => {
        let amenityName = $('#amenityName').val();
        let id = 1;
        if(amenityName !== '') {
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
        } else {
            alert('Amenity name cannot be empty');
        }
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
            <td>`+ `<button class="btn btn-primary" id="edit` + amenities[i].id +`"> Edit</button> <button class="btn btn-danger" id="delete` + amenities[i].id +`"> Delete</button>` + `</td>
        </tr>`);
    }
}

$(document).on("click", ".btn-primary", function() {
    let amenityString = $(this).attr("id");
    let amenityId = amenityString.substr(4, amenityString.length);
    console.log('Editing: ' + amenityId);
});

$(document).on("click", ".btn-danger", function() {
    let amenityString = $(this).attr("id");
    let amenityId = amenityString.substr(6, amenityString.length);
    console.log('Deleting: ' + amenityId);

    $.ajax({
        url: '../rest/amenity/' + amenityId,
        type: 'DELETE',
        success: function() {
            window.location.href = 'amenities.html';
        },
        error : function () {
            alert("Couldn't delete amenity");
        }
    });
});