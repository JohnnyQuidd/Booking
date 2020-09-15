$(document).ready(() => {
    fetchReservations();

    $('#post').click(() => {
        let text = $('#text').val();
        let rating = $('#rating').val();
        let username = localStorage.getItem('username');
        let apartmentId = localStorage.getItem('reviewApartmentId');

        if(text !== '') {
            let payload = JSON.stringify({text,rating, username, apartmentId});
            $.post({
                url: '../rest/comment',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    alert(response);
                },
                error: response => {
                    alert(response);
                }
            });
        } else {
            alert('Comment has to contain text');
        }

    });
});

function fetchReservations() {
    let username = localStorage.getItem('username');
    $.get({
        url: '../rest/reservation/review/' + username,
        dataType: 'json',
        success: response => {
            appendReservationTable(response);
        },
        error: err => {
            alert(err);
        }
    });
 
}

function appendReservationTable(reservations) {
    let i;
    for(i=0; i<reservations.length; i++) {
        $('#reservationsTable tr:last').after(`
            <tr>
                <th scope="row">` + (i+1) + ` </th>
                <td>` + reservations[i].apartmentName +`</td>
                <td>` + reservations[i].message +`</td>
                <td>` + prettifyDate(reservations[i].rentFrom) +`</td>
                <td>` + prettifyDate(reservations[i].rentUntil) +`</td>
                <td>` + reservations[i].reservationStatus +`</td>
                <td><button id="`+ reservations[i].apartmentId + `" class="btn btn-primary" data-toggle="modal" data-target="#reviewModal"> Review</button></td>
            </tr>
        `);
    }
}

function prettifyDate(date) {
    return date.dayOfMonth + "/" + date.monthValue + "/" + date.year;
}

$(document).on('click', '.btn-primary', function () {
    let id = $(this).attr('id');
    localStorage.setItem('reviewApartmentId', id);
});