"""anyway_backend URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf.urls import url, include
from django.db import router

from accounts import views
from django.urls import path


urlpatterns = [
    path('accounts/', views.account_list),
    path('accounts/<int:pk>/', views.account),
    path('login/', views.login),
    path('app_login/', views.app_login),
    path('app_register/', views.app_register),
    path('app_check_id/', views.app_check_id),
    #url(r'^api-v1/', include(router.urls)),
    url(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework'))

]