$(document).ready(() => {
	$('#register').click(() => {
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

		if(validForm(username, firstName, lastName, password, password2)) {
			let data = JSON.stringify({username, firstName, lastName, password,gender});
			$.post({
				url: '../rest/user/register',
				data: data,
				contentType: 'application/json',
				dataType: 'text',
				success: function(response) {
					window.location.href = 'login.html';
				},
				error: function(error) {
					console.log('An Error occurred ');
				}
			});
		} else {
			showWarnings(username, firstName, lastName, password, password2);
		}
	});

	function validForm(username, firstName, lastName, password, password2) {
		return username !== '' && firstName !== '' && lastName !== '' && password !== '' && password2 === password;
	}

	function showWarnings(username, firstName, lastName, password, password2) {
		username === '' ? $('#username-label').show() : $('#username-label').hide();
		firstName === '' ? $('#firstName-label').show() : $('#firstName-label').hide();
		lastName === '' ? $('#lastName-label').show() : $('#lastName-label').hide();
		password === '' ? $('#password-label').show() : $('#password-label').hide();
		password2 !== password ? $('#password2-label').show() : $('#password2-label').hide();
	}
});