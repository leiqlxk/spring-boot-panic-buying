<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>购买产品测试</title>
        <script type="text/javascript" src="jquery.min.js"></script>
        <script type="text/javascript">
            /*var params = {
              userId: 1,
              productId: 1,
              quantity: 3
            }

            $.post('./purchase', params, function (result) {
                alert(result.message)
            })*/
           for (var i = 1; i <= 100; i++ ) {
             var params = {
               userId: 1,
               productId: 1,
               quantity: 1
             }

             $.post('./purchase', params, function (result) {
               // alert(result.message)
             })
           }
        </script>
    </head>
    <body>
        <h1>抢购产品测试</h1>
    </body>
</html>