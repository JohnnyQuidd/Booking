$(document).ready(() => {
    let username = localStorage.getItem('username');
    fetchDataForUser(username);

	$('#submit').click(() => {
        let username = $('#username').val();
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
                    console.log('Success');
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

});

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

function setFields(user) {
    $('#username').val(user.username);
    $('#firstName').val(user.firstName);
    $('#lastName').val(user.lastName);
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