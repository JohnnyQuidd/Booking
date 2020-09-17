$(document).ready(() => {
    fetchUsers();
    fetchReservations();
    fetchOtherReservations();
    fetchPendingComments();
    fetchHostData();

    $('#addApartment').click(() => {
        window.location.href = 'addingApartment.html';
    });

    $('#searchButton').click(() => {
        let hostUsername = localStorage.getItem('username');
        let username = $('#searchField').val();
		
        $.get({
            url: '../rest/host/' + hostUsername + '/' + username,
            dataType: 'json',
            success: response => {
                appendTable(response);
            },
            error: err => {
				console.log('Error ' + err);
            }
        });
    });

    $('#sort').click(() => {
        let criteria = $('#criteria').val();
        let username = localStorage.getItem('username');

        $.get({
            url: '../rest/reservation/sort/' + username + '/' + criteria,
            contentType: 'application/json',
            dataType: 'json',
            success: response => {
                $('#otherReservationsTable tr').empty();
                appendOtherReservationTable(response);
            },
            error: response => {
                console.log(response);
            }
        });
    });

    $('#filter').click(() => {
        let status = $('#filter-select').val();
        $.get({
            url: '../rest/reservation/filter/' + status,
            dataType: 'json',
            success: response => {
                $('#otherReservationsTable tr').empty();
                appendOtherReservationTable(response);
            }, 
            error: response => {
                console.log(response);
            }
        });
    });

    $('#edit').click(() => {
        let username = localStorage.getItem('username');
        let firstName = $('#firstName').val();
        let lastName = $('#lastName').val();
        let password = $('#password').val();
        let password2 = $('#password2').val();

        if(firstName !== '' && lastName !== '' && password !== '' && password2 !== '') {
            if(password !== password2) {
                alert('Passwords must match');
                return;
            }

            let payload = JSON.stringify({username, firstName, lastName, password});
            $.ajax({
                url: '../rest/host',
                type: 'PUT',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    alert(response);
                    window.location.href = 'hostProfile.html';
                },
                error: response => {
                    alert(response);
                }
            });
        }
        else {
            alert('All fields must be populated');
        }
    });

});

function fetchHostData() {
    let username = localStorage.getItem('username');
    $.get({
        url: '../rest/host/' + username,
        dataType: 'json',
        success: response => {
            setHostModal(response);
        },
        error: response => {
            console.log(response);
        }
    });
}

function setHostModal(host) {
    $('#username').val(host.username);
    $('#firstName').val(host.firstName);
    $('#lastName').val(host.lastName);
}

function fetchPendingComments() {
    let hostUsername = localStorage.getItem('username');
    $.get({
        url: '../rest/comment/created/host/' + hostUsername,
        dataType: 'json',
        success: response => {
            renderPendingComments(response);
        },
        error: response => {
            console.log(response);
        }
    });
}

function renderPendingComments(comments) {
    let i;
    for(i=0; i<comments.length; i++) {
        $('#commentsTable tr:last').after(`
        <tr>
            <th scope="row">` + (i+1) + ` </th>
            <td>` + comments[i].username +`</td>
            <td>` + comments[i].apartmentName +`</td>
            <td>` + comments[i].text +`</td>
            <td>` + comments[i].rating +`/10</td>
            <td> <button class="btn btn-success" id="cmtApr` + comments[i].id +`"> Approve</button>
            <button class="btn btn-danger" id="cmtDec` + comments[i].id +`"> Decline</button> </td>
        </tr>
    `);
    }
}

function fetchOtherReservations() {
    let hostUsername = localStorage.getItem('username');
    $.get({
        url: '../rest/reservation/other/' + hostUsername,
        dataType: 'json',
        success: response => {
            appendOtherReservationTable(response);
        },
        error: err => {
            console.log(err);
        }
    });
 
}

function appendOtherReservationTable(reservations) {
    $('.otherReservationsTable-div').empty();
    $('.otherReservationsTable-div').append(`
        <div class="other-reservations-section">
        <table class="table table-hover" id="otherReservationsTable">
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
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody id="otherReservations-body">
            </tbody>
        </table>
    </div>
    `);
    let i;

    for(i=0; i<reservations.length; i++) {
        $('#otherReservationsTable tr:last').after(`
            <tr>
                <th scope="row">` + (i+1) + ` </th>
                <td>` + reservations[i].username +`</td>
                <td>` + reservations[i].apartmentName +`</td>
                <td>` + reservations[i].message +`</td>
                <td>` + prettifyDate(reservations[i].rentFrom) +`</td>
                <td>` + prettifyDate(reservations[i].rentUntil) +`</td>
				<td>` + reservations[i].price +` $</td>
                <td>` + reservations[i].reservationStatus +`</td>
                <td>` + renderFinishedButton(reservations[i]) +`</td>
            </tr>
        `);
    }
}

function renderFinishedButton(reservation) {
    if(reservation.reservationStatus !== 'ACCEPTED') return '';

    return `<button class="btn btn-light" id="${reservation.id}"> Mark as finished</button>`;
}

function fetchReservations() {
    let hostUsername = localStorage.getItem('username');
    $.get({
        url: '../rest/reservation/' + hostUsername,
        dataType: 'json',
        success: response => {
            appendReservationTable(response);
        },
        error: err => {
            console.log(err);
        }
    });
 
}

function appendReservationTable(reservations) {
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
                <td><button id="accept`+ reservations[i].id + `" class="btn btn-success"> Accept</button> 
                <button id="decline`+ reservations[i].id + `" class="btn btn-danger"> Decline</button></td>
            </tr>
        `);
    }
}

function prettifyDate(date) {
    return date.dayOfMonth + "/" + date.monthValue + "/" + date.year;
}

function fetchUsers() {
    let hostUsername = localStorage.getItem('username');
    $.get({
        url: '../rest/host/' + hostUsername + "/users",
        dataType: 'json',
        success: response => {
            appendTable(response);
        },
        error: err => {
            console.log(err);
        }
    });
}

function appendTable(users) {
    $('.usersTable-div').empty();
    $('.usersTable-div').append(`
            <table class="table table-hover" id="usersTable">
            <thead class="thead-inverse">
            <tr>
                <th scope="col">#</th>
                <th scope="col">Username </th>
                <th scope="col">First Name</th>
                <th scope="col">Last Name</th>
                <th scope="col">Gender</th>
                <th scope="col">Active</th>
            </tr>
            </thead>
            <tbody id="table-body">
            </tbody>
        </table>
    `);
    let i;
    for (i=0; i < users.length; i++) {
        $('#usersTable tr:last').after(`
        <tr>
            <th scope="row">` + (i+1) + `</th>
            <td> ` + users[i].username + `</td>
            <td>`+ users[i].firstName + `</td>
            <td>`+ users[i].lastName + `</td>
            <td>`+ users[i].gender + `</td>
            <td>`+ (users[i].active ? `Yes` : `No`) + `</td>
        </tr>`);
    }
}

$(document).on('click', '.btn-light', function() {
    let id = $(this).attr('id');
    
    $.ajax({
        url: '../rest/reservation/finish/' + id,
        type: 'PUT',
        dataType: 'text',
        success: response => {
            alert(response);
            window.location.href = 'hostProfile.html';
        },
        error: response => {
            alert(response);
        }

    });
});

$(document).on("click", ".btn-danger", function() {
    let instruction = $(this).attr("id");

    if(instruction.substr(0,6) === 'cmtDec') {
        let commentID = instruction.substr(6, instruction.length);
        $.ajax({
            url: '../rest/comment/decline/' + commentID,
            type: 'PUT',
            dataType: 'text',
            success: response => {
                alert(response);
				window.location.href = 'hostProfile.html';
            },
            error: response => {
                alert(response);
            }
        });
    } 
    else {
        let requestID = instruction.substr(7, instruction.length);

        $.ajax({
            url: '../rest/reservation/decline/' + requestID,
            type: 'PUT',
            success: function(response) {
                alert(response)
                window.location.href = 'hostProfile.html';
            },
            error : function (response) {
                alert(response.responseText);
            }
        });
    }


});

$(document).on("click", ".btn-success", function() {
    let instruction = $(this).attr("id");

    if(instruction.substr(0,6) === 'cmtApr') {
        let commentID = instruction.substr(6, instruction.length);
        $.ajax({
            url: '../rest/comment/approve/' + commentID,
            type: 'PUT',
            dataType: 'text',
            success: response => {
                alert(response);
				window.location.href = 'hostProfile.html';
            },
            error: response => {
                alert(response);
            }
        });
    }

    else {
        let requestID = instruction.substr(6, instruction.length);

        $.ajax({
            url: '../rest/reservation/accept/' + requestID,
            type: 'PUT',
            success: function(response) {
                alert(response)
                window.location.href = 'hostProfile.html';
            },
            error : function (response) {
                alert(response.responseText);
            }
        });
    }

});