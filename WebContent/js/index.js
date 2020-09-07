$(document).ready(() => {
    $('.date').flatpickr({  
        mode: "multiple",
        dateFormat: "d/m/Y"
    });
    let apartments;
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

    $('#sortApartments').click(() => {
        let criteria = $('#criteria').val();
		let apartments = this.apartments;
        let payload = JSON.stringify({criteria, apartments});

        $.post({
            url: 'rest/apartment/sort',
            data: payload,
            contentType: 'application/json',
            dataType: 'json',
            success: response => {
                apartments = response;
                console.log('Apartments sorted successfully');
                reRenderApartments(apartments);
            },
            error: () => {
                console.log('Couldn\'t filter apartments');
            }
        });

    });


    $('#search').click(() => {
        let priceMin = $('#priceMin').val();
        let priceMax = $('#priceMax').val();
        let numberOfRoomsMin = $('#numberOfRoomsMin').val();
        let numberOfRoomsMax = $('#numberOfRoomsMax').val();
        let numberOfGuests = $('#numberOfGuests').val();
        let city = $('#city').val();
        let availableDatesForRenting = $('.date').val();

        // String of two dates (for instance "02/09/2020, 04/09/2020") has length 22
        if(availableDatesForRenting.length > 0 && availableDatesForRenting.length != 22) {
            alert('Only two dates allowed. Check in and check out');
			return;
        }

        let payload = JSON.stringify({priceMin, priceMax, numberOfRoomsMin, numberOfRoomsMax, numberOfGuests, city, availableDatesForRenting});
        $.post({
            url: 'rest/apartment/advancedSearch',
            data: payload,
            contentType: 'application/json',
            dataType: 'json',
            success: response => {
                apartments = response;
                reRenderApartments(apartments);
            },
            error: response => {
                console.log('Couldn\'t perform advanced search: ' + response);
            }
        });
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
            apartments = response;
            appendApartment(apartments);
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


function reRenderApartments(apartments) {
    $('.apartments').empty();
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