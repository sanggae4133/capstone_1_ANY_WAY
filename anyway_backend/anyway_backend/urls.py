from django.conf.urls import url
from django.urls import path, include
from django.contrib.auth.models import User
from rest_framework import routers, serializers, viewsets
from addresses import views
from django.contrib import admin


urlpatterns = [
    path('login/',views.login),
    path('addresses/<int:pk>/',views.address),
    path('addresses/',views.addresses_list),
    path('login/',views.login),
    path('admin/',admin.site.urls),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('app_login/',views.app_login),
    path('like_list/',views.like_list),
    path('save_list/',views.save_list),
    path('app_register/', views.app_register),
    path('app_check_id/', views.app_check_id),
    path('like_list/delete/',views.list_delete)
]