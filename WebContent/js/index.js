$(document).ready(() => {

    renderButtons();

    $('#loginButton').click(() => {
        window.location.href = 'html/login.html';
    });

    $('#registerButton').click(() => {
        window.location.href = 'html/register.html';
    });

    $('#profileButton').click(() => {
        window.location.href = 'html/profile.html';
    });

	$('#logoutButton').click(() => {
		let username = localStorage.getItem('username');
		let payload = JSON.stringify(username);
		$.post({
                url: '/Booking/rest/user/logout',
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    localStorage.clear();
					window.location.href = 'index.html';
                },
                error: err => {
                    console.log(err);
                }
            });
	});
});

function renderButtons() {
    if(localStorage.getItem('username') === null) {
        $('#loginButton').show();
        $('#registerButton').show();
		$('#profileButton').hide();
		$('#logoutButton').hide();
    } else {
	    $('#loginButton').hide();
        $('#registerButton').hide();
		$('#profileButton').show();
		$('#logoutButton').show();
	}
}