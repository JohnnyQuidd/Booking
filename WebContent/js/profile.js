$(document).ready(() => {
    let username = localStorage.getItem('username');
    fetchDataForUser(username);
    fetchReservations();

	$('#submit').click(() => {
        let username = localStorage.getItem('username');
        let firstName = $('#firstName').val();
        let lastName = $('#lastName').val();
        let password = $('#password').val();
        let password2 = $('#password2').val();


		if(validForm(firstName, lastName, password, password2)) {
            let payload = JSON.stringify({username, firstName, lastName, password});
            $.ajax({
                url: '../rest/user/' + username,
				type: 'PUT',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    alert(response);
                    window.location.href = 'profile.html';
                },
                error: err => {
					console.log('Error');
                    alert(err.responseText);
                }
            });
        } else {
            showWarnings(firstName, lastName, password, password2);
        }
    });

    $('#sort').click(() => {
        let criteria = $('#criteria').val();
        let username = localStorage.getItem('username');

        $.get({
            url: '../rest/reservation/sort/user/' + username + '/' + criteria,
            contentType: 'application/json',
            dataType: 'json',
            success: response => {
                appendReservationTable(response);
            },
            error: response => {
                console.log(response);
            }
        });
    });
    

    $('#review').click(() => {
        window.location.href = 'review.html';
    });

});

$(document).on('click', '.btn-danger', function() {
    let instruction = $(this).attr("id");
    let requestID = instruction.substr(6, instruction.length);

    $.ajax({
        url: '../rest/reservation/cancel/' + requestID,
        type: 'PUT',
        success: function(response) {
            alert(response)
            window.location.href = 'profile.html';
        },
        error : function (response) {
            alert(response.responseText);
        }
    });
});

function fetchReservations() {
    let username = localStorage.getItem('username');
    $.get({
        url: '../rest/reservation/user/' + username,
        dataType: 'json',
        success: response => {
            appendReservationTable(response);
        },
        error: err => {
            alert(err.responseText);
        }
    });
 
}

function appendReservationTable(reservations) {
    $('.reservationsTable-div').empty();
    $('.reservationsTable-div').append(`
        <table class="table table-hover" id="reservationsTable">
            <thead class="thead-inverse">
                <tr>
                <th scope="col">#</th>
                <th scope="col">Username </th>
                <th scope="col">Apartment</th>
                <th scope="col">Message</th>
                <th scope="col">From</th>
                <th scope="col">Until</th>
                <th scope="col">Price</th>
                <th scope="col">Status</th>
                </tr>
            </thead>
            <tbody id="reservations-body">
            </tbody>
        </table>
    `);
    let i;
    for(i=0; i<reservations.length; i++) {
        $('#reservationsTable tr:last').after(`
            <tr>
                <th scope="row">` + (i+1) + ` </th>
                <td>` + reservations[i].username +`</td>
                <td>` + reservations[i].apartmentName +`</td>
                <td>` + reservations[i].message +`</td>
                <td>` + prettifyDate(reservations[i].rentFrom) +`</td>
                <td>` + prettifyDate(reservations[i].rentUntil) +`</td>
                <td>` + reservations[i].price +` $</td>
                <td>` + showStatus(reservations[i].reservationStatus, reservations[i].id) + `</td>
            </tr>
        `);
    }
}

function prettifyDate(date) {
    return date.dayOfMonth + "/" + date.monthValue + "/" + date.year;
}

function showStatus(status, id) {
    if(status === 'CREATED')
        return "<button id='delete"+id+ "' class='btn btn-danger'> Cancel request</button>";
    return status;
}

function fetchDataForUser(username) {
    $.get({
        url: '../rest/user/' + username,
        dataType: 'json',
        success: response => {
            setFields(response);
        },
        error: err => {
            alert(err.responseText);
        }
    });
}

function setFields(userList) {
    $('#username').val(userList[0].username);
    $('#firstName').val(userList[0].firstName);
    $('#lastName').val(userList[0].lastName);
}

function validForm(firstName, lastName, password, password2) {
    return firstName !== '' && lastName !== '' && password !== '' && password2 === password;
}

function showWarnings(firstName, lastName, password, password2) {
    firstName === '' ? $('#firstName-label').show() : $('#firstName-label').hide();
    lastName === '' ? $('#lastName-label').show() : $('#lastName-label').hide();
    password === '' ? $('#password-label').show() : $('#password-label').hide();
    password2 !== password ? $('#password2-label').show() : $('#password2-label').hide();
}