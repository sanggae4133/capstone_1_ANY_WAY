# Generated by Django 2.2.6 on 2021-12-09 05:44

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('addresses', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='SaveList',
            fields=[
                ('useremail', models.ForeignKey(db_column='useremail', on_delete=django.db.models.deletion.DO_NOTHING, primary_key=True, serialize=False, to='addresses.Users')),
                ('startlocation', models.CharField(max_length=20)),
                ('endlocation', models.CharField(max_length=20)),
            ],
            options={
                'db_table': 'save_list',
                'managed': False,
            },
        ),
    ]
