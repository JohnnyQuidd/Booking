$(document).ready(() => {
    fetchUsers();
    fetchReservations();
    fetchOtherReservations();
    fetchPendingComments();

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
                printUser(response);
            },
            error: err => {
				console.log('Error ' + err);
                alert(err.responseText);
            }
        });
    });
});

function fetchPendingComments() {
    let hostUsername = localStorage.getItem('username');
    $.get({
        url: '../rest/comment/created/host/' + hostUsername,
        dataType: 'json',
        success: response => {
            renderPendingComments(response);
        },
        error: response => {
            alert(response);
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
            alert(err.responseText);
        }
    });
 
}

function appendOtherReservationTable(reservations) {
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
                <td>` + reservations[i].reservationStatus +`</td>
            </tr>
        `);
    }
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
            alert(err.responseText);
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
            alert(err.responseText);
        }
    });
}

function appendTable(users) {
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

function printUser(users) {
    let user = users[0];
    $("#usersTable tr:last").empty();

    $('#usersTable tr:last').after(`
    <tr>
        <th scope="row">` + 1 + `</th>
        <td> ` + user.username + `</td>
        <td>`+ user.firstName + `</td>
        <td>`+ user.lastName + `</td>
        <td>`+ user.gender + `</td>
        <td>`+ (user.active ? `Yes` : `No`) + `</td>
    </tr>`);
}

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