$(document).ready(() => {
    fetchAmenities();
    fetchApartment();
    drawMap();

    counter = 0;
    imagesData = [];
    coordinates = [-99, -99];

	$('.date').flatpickr({  
        mode: "multiple",
        dateFormat: "d/m/Y"
	});


    $('#postApartment').click(() => {
        let apartmentName = $('#apartmentName').val();
        let numberOfRooms = $('#numberOfRooms').val();
        let numberOfGuests = $('#numberOfGuests').val();
        let apartmentType = $('#apartmentType').val();
        if(apartmentType === 'Full Apartment') apartmentType = 'FULL_APARTMENT';
        if(apartmentType === 'Room') apartmentType = 'ROOM';
        let pricePerNight = $('#pricePerNight').val();
        let amenities = $('#amenities').val();
        let street = $('#street').val();
        let number = $('#number').val();
        let city = $('#city').val();
        let zipCode = $('#zipCode').val();
        // var images are sent to server
		var images = [];
        var imgs = document.getElementById('imgs').getElementsByTagName('img');
        let availableDatesForRenting = $('.date').val();
        for (var i = 0; i < imgs.length; i++) {
            images.push(imgs[i].src)
        }



        if(validFields(apartmentName, numberOfRooms, numberOfGuests, apartmentType, pricePerNight, amenities, street,
            number, city, zipCode, coordinates, images, availableDatesForRenting)) {
                let latitude = coordinates[0];
                let longitude = coordinates[1];
                let id = localStorage.getItem('apartmentId');
                let apartmentDTO = JSON.stringify({id, apartmentName, numberOfRooms, numberOfGuests, 
                apartmentType, pricePerNight, amenities, street, number, 
                city, zipCode, latitude, longitude, images, availableDatesForRenting});
                

                $.post({
                    url: '../rest/apartment/modify',
                    data: apartmentDTO,
                    contentType: 'application/json',
                    dataType: 'text',
                    success: response => {
                        alert('Apartment modified successfully');
                    },
                    error: err => {
                        console.log(err.responseText);
                    }
                });
        }

        else {
            alert('Fields are not valid');
        }

    });
});

function fetchApartment() {
    let id = localStorage.getItem('apartmentId');
    $.get({
        url: '../rest/apartment/' + id,
        dataType: 'json',
        success: response => {
            fillInForm(response);
        },
        error: response => {
            console.log('Unable to fetch apartment for modifying');
        }
    });
}

function fillInForm(response) {
    let apartment = response[0];

    $('#apartmentName').val(apartment.apartmentName);
    $('#numberOfRooms').val(apartment.numberOfRooms);
    $('#numberOfGuests').val(apartment.numberOfGuests);
    $('#pricePerNight').val(apartment.pricePerNight);
    $('#street').val(apartment.location.address.street);
    $('#number').val(apartment.location.address.number);
    $('#city').val(apartment.location.address.city);
    $('#zipCode').val(apartment.location.address.zipCode);
}

function fetchAmenities() {
    $.get({
        url: '../rest/amenity',
        dataType: 'json',
        success: response => {
            appendSelectBox(response);
        },
        error: err => {
            alert(err.responseText);
        }
    });
}

function appendSelectBox(amenities) {
    for(let i=0; i<amenities.length; i++) {
        $('#amenities').append($('<option>', {
            value: amenities[i].amenity,
            text: amenities[i].amenity
        }));
    }
}

function validFields(apartmentName, numberOfRooms, numberOfGuests, apartmentType, pricePerNight, amenities, street,
    number, city, zipCode, coordinates, images, availableDatesForRenting) {
        return apartmentName.length > 0 && numberOfRooms > 0 && numberOfGuests > 0 && apartmentType.length > 0 &&
        pricePerNight >= 0 && amenities.length > 0 && street.length > 0 && number >= 0 && city.length > 0 &&
        zipCode >= 0 && images.length > 0 && coordinates[0] !== -99 && coordinates[1] !== -99 &&
        availableDatesForRenting.length > 0;
    }

function drawMap(){
	    var map = new ol.Map({
        target: 'map-placeholder',
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ],
        view: new ol.View({
		  projection: 'EPSG:4326',
          center: [19.84, 45.25],
          zoom: 8
        })
      });

	map.on('click', function(e) {
        coordinates = e.coordinate;
    });
    
    var layer = new ol.layer.Vector({
        source: new ol.source.Vector({
            features: [
                new ol.Feature({
                    geometry: new ol.geom.Point([4.35247, 50.84673])
                })
            ]
        })
    });
    map.addLayer(layer);
};

function showimg() {
    var reader = new FileReader();
    if (document.getElementById('img').files[0]) reader.readAsDataURL(document.getElementById('img').files[0]);

    reader.onloadend = function () {
        $('#imgs').append("<img src=\"" + reader.result + "\" id=\"img" + counter + "\">");
        imagesData[counter] = document.getElementById('img').files[0];
        counter += 1;
    }
}