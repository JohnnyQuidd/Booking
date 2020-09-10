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

    $('#amenities').click(() => {
        window.location.href = 'amenities.html';
    });

    $('#apartmentsButton').click(() => {
        window.location.href = 'managingApartments.html';
    });

    $('#addHost').click(() => {
        let username = $('#username').val();
        let firstName = $('#firstName').val();
        let lastName = $('#lastName').val();
        let password = $('#password').val();
        let password2 = $('#password2').val();
        let gender = '';
		
		if($('#male').is(':checked')){
			gender = 'Male';
		} else {
			gender = 'Female';
        }
        if(allFieldsSatisfied(username, firstName, lastName, password, password2)) {
           if(password !== password2) {
               alert('Passwords have to match');
                return;
           }
           let payload = JSON.stringify({username, firstName, lastName, password, gender});

           $.post({
                url: '../rest/host',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: () => {
                    alert('Host successfully added');
                },
                error : err => {
                    alert(err.responseText);
                }
           });
        }
        else {
            alert('All fields must be filled');
        }
        
        
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
            <td>` + renderActionButton(users[i].active, users[i].username) +` </td>
        </tr>`);
    }
}

function renderActionButton(active, username) {
    if(active === true) {
        return `<button id="block`+ username + `" class="btn btn-danger"> Block </button>`;
    }
    return `<button id="unblock`+ username + `" class="btn btn-success"> Unblock </button>`;
}

$(document).on("click", ".btn-danger", function() {
    let instruction = $(this).attr("id");
    let username = instruction.substr(5, instruction.length);

    $.ajax({
        url: '../rest/user/block/' + username,
        type: 'POST',
        success: function() {
            window.location.href = 'adminProfile.html';
        },
        error : function () {
            alert("Couldn't block user");
        }
    });
});

$(document).on("click", ".btn-success", function() {
    let instruction = $(this).attr("id");
    let username = instruction.substr(7, instruction.length);
    console.log('Unblocking: ' + username);

    $.ajax({
        url: '../rest/user/unblock/' + username,
        type: 'POST',
        success: function() {
            window.location.href = 'adminProfile.html';
        },
        error : function () {
            alert("Couldn't unblock user");
        }
    });
});

function allFieldsSatisfied(username, firstName, lastName, password, password2) {
    return username !== '' && firstName !== '' && lastName !== '' && password !== '' && password2 !== '';
}