$(document).ready(() => {
    fetchApartments();
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

$(document).on("click", ".btn-secondary", function() {
    let apartmentId = $(this).attr("id");
    localStorage.setItem('apartmentId', apartmentId);
    window.location.href = 'html/apartmentDetails.html';
});


function fetchApartments() {
    $.get({
        url: 'rest/apartment/all',
        dataType: 'json',
        success: response => {
            appendApartment(response);
            console.log('Successfully fetched apartments');
        },
        error: err => {
            alert(err.responseText);
        }
    });
}

function appendApartment(apartments) {
    for(let i=0; i<apartments.length; i++) {
        $('.apartments').append(
            '<div class="apartment">' 
            + '<img class="apartmentPreview" src="' + apartments[i].images[0] + '"/>'
            + '<p class="apartmentName"> Apartment: ' + apartments[i].apartmentName + '</p>' 
            + '<p class="numberOfGuests"> Guests: ' + apartments[i].numberOfGuests + '</p>'
            + '<p class="numberOfRooms"> Rooms: ' + apartments[i].numberOfRooms + '</p>'
            + '<p class="pricePerNight"> Price per night: ' + apartments[i].pricePerNight + ' $</p>'
            + '<p class="location"> Location: ' + apartments[i].location.address.street + ', ' + apartments[i].location.address.city + '</p>'
            + '<button class="btn btn-secondary" id="' + apartments[i].id +'">' + ' More information'  + '</button>'
            +'</div>'
        );
    }
}


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