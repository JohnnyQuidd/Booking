$(document).ready(() => {
    fetchUsers();

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

function printUser(user) {
    $("#usersTable tr").empty();

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