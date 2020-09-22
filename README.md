
## What is Booking?

Booking is a Web application for renting apartments. It supports leaving reviews, filtering apartments, searching for desired apartments on the market by applying wide range of different criteria and so on. Project was an official assignment of Web programming course that took place in Faculty of technical sciences in Novi Sad, 2020.

<hr>
##Technology stack

<ul>
    <li> JavaEE using JAX-RS specification</li>
    <li> jQuery  & Ajax </li>
</ul>

<hr>
### Deployment guide

Booking is a project developed for deploying on Tomcat container (preferable version is _Tomcat 8.0.x_)

<ol>
<li> After cloning repository, open Eclipse and import a project </li>
<li> Export project as a .war file anywhere in your file system</li>
<li> Navigate to your Tomcat home directory under and copy .war file under <code> tomcat-dir/webapps </code> </li>
</ol>

To start a Tomcat container with an app deployed, go to <code> tomcat-dir/bin </code> and start a container. If you are on a Linux, run:
```
    sudo bash ./catalina.sh start
```

Tomcat container should be up and running, which can be seen by navigating to: **http:localhost:8080/Booking**. If everything is properly set up, you will be greeted by a Booking home page.

To stop a container, run a following command:

```
    sudo bash ./catalina.sh stop
```
Only predefined user is admin with **username:** admin and **password:** admin
Admin can create hosts that can further create new apartment which users can rent.

**Please note**
Internet connection is required because external API's are being called such as *OpenLayers* for generating dynamic maps.

<hr>
### Some insights of project's Look and Feel

#####Homepage for non-logged in user

![Booking homepage](/images/homepage.png "Booking homepage")

#####Adding apartments from host's perspective

![Adding apartment](/images/addingApartment1.png "Adding apartment1")

![Adding apartment](/images/addingApartment2.png "Adding apartment2")

##### Host's homepage

![#eservations](/images/reservations.png "Adding apartment2")


##### Adding amenities from admin's perspective

![Adding amenities](/images/amenities.png "Adding amenities")

##### Admin adding a new Host

![Adding host](/images/addingHost.png "Adding host")



##### Comments on a advertisement

![Comments](/images/comments.png "Adding apartment2")


<hr>
### Author
<ul>
    <li> Petar Kovacevic RA 245/2017 </li>
</ul>
