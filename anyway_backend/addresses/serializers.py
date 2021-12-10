from rest_framework import  serializers
from .models import Addresses
from .models import Users
from .models import LikeList


class AddressesSerializer(serializers.ModelSerializer):
    class Meta:
        model = Addresses
        fields = ['name','phone_number','address']


class UsersSerializer(serializers.ModelSerializer):
    class Meta:
        model = Users
        fields = ['username','useremail','userpw']


class LikeListSerializer(serializers.ModelSerializer):
    class Meta:
        model = LikeList
        fields = ['userid','location']


class Serializer(serializers.Serializer):
    likename = serializers.CharField(max_length=140)
    location = serializers.CharField()

