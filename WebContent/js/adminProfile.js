$(document).ready(() => {
    fetchUsers();
    
    $('#searchButton').click(() => {
        let username = $('#searchField').val();
		
        $.get({
            url: '../rest/user/' + username,
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
    $.get({
        url: '../rest/user/all',
        dataType: 'json',
        success: response => {
            appendTable(response);
        },
        error: err => {
            alert(err.responseText);
        }
    });
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

/*
    <tr>
        <th scope="row">1</th>
        <td>Skinny Pete</td>
        <td>Petar</td>
        <td>Kovacevic</td>
        <td>Male</td>
        <td>Yes</td>
    </tr>
 */