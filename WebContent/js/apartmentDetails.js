$(document).ready(() => {
    fetchApartment();

    $('.date').flatpickr({  
        dateFormat: "d/m/Y"
    });
    
    $('#sendRentRequest').click(() => {
        let username = localStorage.getItem('username');
        let apartmentId = localStorage.getItem('apartmentId');
        let message = $('#message').val();
        let numberOfNights = $('#numberOfNights').val();
        let date = $('.date').val();

        if(validFields(username, apartmentId, message, numberOfNights, date)) {
            let payload = JSON.stringify({username, apartmentId, message, numberOfNights, date});
            
            $.post({
                url: '../rest/reservation',
                data: payload,
                contentType: 'application/json',
                dataType: 'text',
                success: response => {
                    alert(response);
                },
                error: response => {
                    alert(response.responseText);
                }
            });
        }
        else {
            alert('Invalid fields');
        }

    });
});

function validFields(username, apartmentId, message, numberOfNights, date) {
    return username.length > 0 && apartmentId !== undefined && message.length > 0
            && numberOfNights >= 0 && date.length > 0;
}

$(document).on("click", ".btn-success", function() {
    let id = localStorage.getItem('apartmentId');
		console.log(id);
        $.ajax({
			method: 'PUT',
            url: '../rest/apartment/activate/' + id,
            dataType: 'text',
            success: response => {
                alert('Apartment successfully activated');
				window.location.href = "adminProfile.html";
            },
            error: response => {
                alert('Couldn\'t activate apartment as of now');
            }
        });
});

$(document).on("click", ".btn-danger", function() {
    let id = localStorage.getItem('apartmentId');
		console.log(id);
        $.ajax({
			method: 'DELETE',
            url: '../rest/apartment/' + id,
            dataType: 'text',
            success: response => {
                alert('Apartment successfully deleted');
				window.location.href = "../index.html";
            },
            error: response => {
                alert('Couldn\'t delete apartment as of now');
            }
        });
});

$(document).on("click", ".btn-light", function() {
    window.location.href = "modifyApartment.html";
	
});


function fetchApartment() {
    let apartmentID = localStorage.getItem('apartmentId');
    $.get({
        url: '../rest/apartment/' + apartmentID,
        dataType: 'json',
        success: response => {
            renderApartment(response);
        },
        error: err => {
            alert(err);
        }
    });
}

function renderApartment(apartmentList) {
    let apartment = apartmentList[0];
    let address = apartment.location.address;
    let apartmentType;
    let profileImageSrc = apartment.images[0];
    apartment.apartmentType === 'FULL_APARTMENT'? apartmentType = 'Full Apartment' : apartmentType = 'Room';
    $('.container').append(
        '<h2 id="apartmentName">' + apartment.apartmentName +'</h2>'
        + '<p>' + renderAppropriateButton(apartment.status, apartment.hostName) + "</p>"
        + '<p id="apartmentType">Apartment type: ' + apartmentType + '</p>'
        + '<p id="numberOfRooms">Rooms: ' + apartment.numberOfRooms + '</p>'
        + '<p id="numberOfGuests">Guests: ' + apartment.numberOfGuests + '</p>'
        + '<p id="pricePerNight">Price per night: ' + apartment.pricePerNight + '$</p>'
        + '<p id="hostName">Posted by: ' + apartment.hostName + '</p>'
        + '<p id="street">Street: ' + address.street + ' ' + address.number + '</p>'
        + '<p id="city">City: ' + address.city + ' ' + address.zipCode + '</p>'
        + '<p id="location">Long: ' + apartment.location.lattitude.toPrecision(8) + '° Lat: ' + apartment.location.longitude.toPrecision(8) + '°' + '</p>'
    );


    for(let i=0; i<apartment.images.length; i++) {

        if(i === 0) {
            $('.carousel-indicators').append(
                '<li data-target="#myCarousel" data-slide-to="'+ i +'" class="active"></li>'
            );

            $('.carousel-inner').append(
                '<div class="carousel-item active">'
                + '<img src="' + apartment.images[i] + '" alt="First slide"/>'
                + '</div>'
            );
        } else {
            $('.carousel-indicators').append(
                '<li data-target="#myCarousel" data-slide-to="' + i +'"></li>'
            );
            $('.carousel-inner').append(
                '<div class="carousel-item">'
                + '<img src="' + apartment.images[i] + '"/>'
                + '</div>'
            );
        }
    }

    $('.amenityContainer').append('Amenities: ');
    for(let i=0; i<apartment.amenities.length; i++) {  
        if(i !== apartment.amenities.length-1) {
            $('.amenityContainer').append(apartment.amenities[i].amenity + ', ');
        } else {
            $('.amenityContainer').append(apartment.amenities[i].amenity);
        }
          
    }

    $('.datesContainer').append('Dates available for renting: ');
    for(let i=0; i<apartment.availabeDatesForRenting.length; i++) {
        if(i !== apartment.availabeDatesForRenting.length-1) {
            $('.datesContainer').append(prettifyDate(apartment.availabeDatesForRenting[i]) + ', ');
        } else {
            $('.datesContainer').append(prettifyDate(apartment.availabeDatesForRenting[i]));
        }
    }

    function prettifyDate(date) {
		return date.dayOfMonth + "/" + date.monthValue + "/" + date.year + "";
    }

    function renderAppropriateButton(status, hostName) {
        let buttonString = "";
        let username = localStorage.getItem('username');
        let role = localStorage.getItem('role');
        if(status === "INACTIVE" && role === 'admin') {
            buttonString += '<button class="btn btn-success" id="activateApartment"> Activate apartment</button>';
        }
        if(role === 'admin') {
            buttonString += '<button class="btn btn-danger" id="deleteApartment"> Delete apartment</button>';
            buttonString += '<button class="btn btn-light" id="editApartment"> Edit apartment</button>';
        }
        if(role === 'host' && username === hostName) {
            buttonString += '<button class="btn btn-danger" id="deleteApartment"> Delete apartment</button>';
            buttonString += '<button class="btn btn-light" id="editApartment"> Edit apartment</button>';
        }
        if(status === "ACTIVE" && role === 'user'){
            buttonString += '<button class="btn btn-primary" id="rentRequest" data-toggle="modal" data-target="#rentModal"> Request a rent</button>';
        }
            
        
        return buttonString;
    }
}