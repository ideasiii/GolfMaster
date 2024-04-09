<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Google Maps Example</title>
<style>
    #map {
        width: 100%;
        height: 400px;
    }
</style>
</head>
<body>
    <div id="map"></div>
    <script>
        function initMap() {
            var allowedBounds = new google.maps.LatLngBounds(
                new google.maps.LatLng(-34.0, 150.0), // southwest corner
                new google.maps.LatLng(-31.0, 151.0)  // northeast corner
            );

            var mapOptions = {
                center: allowedBounds.getCenter(),
                zoom: 10,
                streetViewControl: false, // 街道圖
                mapTypeControl: false    // 地圖類控制鍵
            };

            var map = new google.maps.Map(document.getElementById('map'), mapOptions);
            map.setOptions({ minZoom: 10, maxZoom: 15 });

            // 当用户拖动地图时，限制视图保持在允许的边界内
            google.maps.event.addListener(map, 'dragend', function() {
                if (allowedBounds.contains(map.getCenter())) return;

                // 不在允许的边界内时，重置中心
                var c = map.getCenter(),
                    x = c.lng(),
                    y = c.lat(),
                    maxX = allowedBounds.getNorthEast().lng(),
                    maxY = allowedBounds.getNorthEast().lat(),
                    minX = allowedBounds.getSouthWest().lng(),
                    minY = allowedBounds.getSouthWest().lat();

                if (x < minX) x = minX;
                if (x > maxX) x = maxX;
                if (y < minY) y = minY;
                if (y > maxY) y = maxY;

                map.setCenter(new google.maps.LatLng(y, x));
            });

            // 当用户尝试缩放超出允许的范围时，重置缩放级别
            google.maps.event.addListener(map, 'zoom_changed', function() {
                if (map.getZoom() < 10) map.setZoom(10);
                if (map.getZoom() > 15) map.setZoom(15);
            });
        }
    </script>
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCe8z8WkcDN9m2hDHSNTK8to05QVZK1qSw&callback=initMap">
    </script>
</body>
</html>

