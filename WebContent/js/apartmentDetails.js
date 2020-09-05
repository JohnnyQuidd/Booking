$(document).ready(() => {
    fetchApartment();
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

    function prettifyDate(unix_timestamp) {
        var date = new Date(unix_timestamp);
        return date.getDay()+1 + "/" + date.getMonth() + "/" + date.getUTCFullYear(); //date + "/" + hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
    }
}