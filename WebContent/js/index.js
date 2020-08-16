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

    $('#adminProfileButton').click(() => {
        window.location.href = 'html/adminProfile.html';
    });

    $('#hostProfileButton').click(() => {
        window.location.href = 'html/hostProfile.html';
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
        $('#adminProfileButton').hide();
        $('#hostProfileButton').hide();
    } else {
	    $('#loginButton').hide();
        $('#registerButton').hide();
		
        $('#logoutButton').show();
        
        switch(localStorage.getItem('role')) {
            case 'admin' :  $('#adminProfileButton').show();
                            $('#profileButton').hide();
                            $('#hostProfileButton').hide();
                            break;
            case 'user' :  $('#adminProfileButton').hide();
                            $('#profileButton').show();
                            $('#hostProfileButton').hide();
                            break;
            case 'host' :   $('#adminProfileButton').hide();
                            $('#profileButton').hide();
                            $('#hostProfileButton').show();
                            break;
        }
	}
}