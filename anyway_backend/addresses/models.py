from django.db import models

# Create your models here.


class Addresses(models.Model):
    name = models.CharField(max_length=10)
    phone_number = models.CharField(max_length=13)
    address = models.TextField()
    created = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['created']


class LikeList(models.Model):
    useremail = models.ForeignKey('Users', models.DO_NOTHING, db_column='useremail', primary_key=True)
    likename = models.CharField(db_column='likeName', max_length=10, blank=True, null=True)  # Field name made lowercase.
    location = models.CharField(max_length=30)

    class Meta:
        managed = False
        db_table = 'like_list'
        unique_together = (('useremail', 'location'),)


class SaveList(models.Model):
    useremail = models.ForeignKey('Users', models.DO_NOTHING, db_column='useremail', primary_key=True)
    startlocation = models.CharField(max_length=20)
    endlocation = models.CharField(max_length=20)

    class Meta:
        managed = False
        db_table = 'save_list'
        unique_together = (('useremail', 'startlocation', 'endlocation'),)


class Users(models.Model):
    useremail = models.CharField(primary_key=True, max_length=20)
    username = models.CharField(max_length=10, blank=True, null=True)
    userpw = models.CharField(max_length=10, blank=True, null=True)

    class Meta:
        managed = False
        db_table = 'users'
