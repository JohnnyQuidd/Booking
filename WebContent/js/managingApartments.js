$(document).ready(() => {
    fetchInactiveApartments();

    $(document).on("click", ".btn-secondary", function() {
        let apartmentId = $(this).attr("id");
        localStorage.setItem('apartmentId', apartmentId);
        window.location.href = 'apartmentDetails.html';
    });
});

function fetchInactiveApartments() {
    $.get({
        url: '../rest/apartment/inactive',
        dataType: 'json',
        success: response => {
            apartments = response;
            appendInactiveApartment(apartments);
        },
        error: err => {
            alert(err.responseText);
        }
    });
}

function appendInactiveApartment(apartments) {
    for(let i=0; i<apartments.length; i++) {
        $('.inactiveApartments').append(
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