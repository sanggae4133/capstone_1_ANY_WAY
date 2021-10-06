from django.db import models


class Account(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=45)
    userid = models.CharField(db_column='userId', max_length=45)  # Field name made lowercase.
    userpw = models.CharField(db_column='userPw', max_length=45)  # Field name made lowercase.

    class Meta:
        managed = False
        db_table = 'account'
