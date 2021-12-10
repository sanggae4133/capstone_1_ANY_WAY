import json

from django.contrib.auth.models import User
from django.core.exceptions import ObjectDoesNotExist
from django.shortcuts import render
from django.http import HttpResponse, JsonResponse, HttpResponseRedirect
from django.views.decorators.csrf import csrf_exempt
from .models import Addresses, Users, LikeList, SaveList
from .serializers import AddressesSerializer, UsersSerializer, Serializer
from rest_framework.parsers import JSONParser
from django.contrib.auth import authenticate


# Create your views here.
@csrf_exempt
def addresses_list(request):
    if request.method == 'GET':
        query_set = Addresses.objects.all()
        serializer= AddressesSerializer(query_set,many=True)
        return JsonResponse(serializer.data,safe=False)

    elif request.method == 'POST':
        data = JSONParser().parse(request)
        serializer = AddressesSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return JsonResponse(serializer.data, status=201)
        return JsonResponse(serializer.errors, status=400)


@csrf_exempt
##pk=인덱스
def address(request,pk):

    obj =Addresses.objects.get(pk=pk)
    if request.method == 'GET':
        serializer= AddressesSerializer(obj)
        return JsonResponse(serializer.data,safe=False)
    elif request.method=='PUT':
        data = JSONParser().parse(request)
        serializer = AddressesSerializer(obj,data=data)
        if serializer.is_valid():
            serializer.save()
            return JsonResponse(serializer.data, status=201)
        return JsonResponse(serializer.errors, status=400)
    elif request.method=='DELETE':
        obj.delete()
        return HttpResponse(status=204)

@csrf_exempt
def login(request):
    if request.method=='POST':
        data=JSONParser().parse(request)
        search_name=data['name']
        print(search_name)
        obj=Addresses.objects.get(name=search_name)
        print(obj.phone_number)

        if data['phone_number']==obj.phone_number:
            return HttpResponse(status=200)
        else:
            return HttpResponse(status=400)

@csrf_exempt
def login(request):

    if request.method=='POST':
        print('리퀘스트로그'+str(request.body))
        data = JSONParser().parse(request)
        id = data['userid']
        pw = data['userpw']
        print('id = '+id+'  pw = '+pw)

        result = authenticate(username=id,password=pw)
        ##디비값이랑 비교해서 맞으면 유저아이디를 넘겨준다

        if result:
            print('로그인성공!')
            ##user key에 로그인한 유저의 아이디값
            request.session['user'] = id
            return HttpResponse(status=200)
        else:
            print('실패')
            ##보통 실패할때 401,성공 200
            return  HttpResponse(status=401)
    return render(request, 'addresses/login.html')


#addresses/views.py
@csrf_exempt
def app_login(request):

    if request.method == 'POST':
        print("리퀘스트 로그" + str(request.body))
        email = request.POST.get('useremail', '')
        pw = request.POST.get('userpw', '')
        print("id = " + email + " pw = " + pw)

        try:
            obj = Users.objects.get(useremail=email)
        except ObjectDoesNotExist:
            print("ID 없어서 실패")
            return JsonResponse({'code': '1001', 'msg': 'ID 없어서 로그인실패입니다.'}, status=200)

        result = pw == obj.userpw

        if result:
            print("로그인 성공!")
            request.session['user'] = email
            return JsonResponse({'code': '0000', 'msg': '로그인성공입니다.'}, status=200)
        else:
            print("실패")
            return JsonResponse({'code': '1001', 'msg': '로그인실패입니다.'}, status=200)


@csrf_exempt
def like_list(request):
    if request.method=='GET':
        print("리퀘스트 로그" + str(request.body))
        id = request.GET.get('useremail','');
        ## data = json.loads(request.body)
        ##id = data['useremail']
        print(id)
        likelist = LikeList.objects.filter(useremail=id)
        like=list(likelist.values())
        ##result=Serializer(like,many=True)
        ##for like in likelist:
        ##    print(like.location)
        return JsonResponse(like,safe=False)
        ##return HttpResponse(result)

    elif request.method=='POST':
        print("리퀘스트 로그" + str(request.body))
        data = json.loads(request.body)
        ##print("session : " + session_userid)
        likeName=data['likename']
        likelocation= data['location']
        id=data['useremail']
        print(likeName,likelocation)

        ## insert 장고는 FK는 _id가 생긴다 ;;;
        LikeList.objects.create(useremail_id=id, likename=likeName, location=likelocation)
        ## insert 후에는 꼭 redirect 처리!
        JsonResponse({'code': '0000', 'msg': '굿.'}, status=200)

    elif request.method=='DELETE':
        session_userid = request.session.get('user')  # 세션으로부터 유저 정보 가져오기
        print("session : " + session_userid)
        likelocation= request.POST.get('location', '')
        likelist = LikeList.objects.filter(useremail=session_userid ,location=likelocation)
        likelist.delete()
        return HttpResponse



@csrf_exempt
def save_list(request):
    if request.method=='GET':
        session_userid = request.session.get('user') #세션으로부터 유저 정보 가져오기
        print("session : "+session_userid)
        savelist=SaveList.objects.filter(useremail=session_userid)
        data=list(savelist.values())
        for save in savelist:
            print(save.location)
        return JsonResponse(data,safe=False)

    elif request.method=='POST':
        session_userid = request.session.get('user')  # 세션으로부터 유저 정보 가져오기
        print("session : " + session_userid)
        data = JSONParser().parse(request)
        startLocation=data['startlocation']
        endLocation=data['endlocation']
        print(startLocation,endLocation)

        ##insert 장고는 FK는 _id가 생긴다 ;;;
        LikeList.objects.create(useremail_id=session_userid, startlocation=startLocation, endlocation=endLocation)
              # insert 후에는 꼭 redirect 처리!

    elif request.method=='DELETE':
        session_userid = request.session.get('user')  # 세션으로부터 유저 정보 가져오기
        print("session : " + session_userid)
        data = JSONParser().parse(request)
        startLocation = data['startlocation']
        endLocation = data['endlocation']
        savelist = SaveList.objects.filter(useremail=session_userid ,startlocation=startLocation,endlocation=endLocation)
        savelist.delete()



@csrf_exempt
def app_register(request):
    if request.method == 'POST':
        print("리퀘스트 로그" + str(request.body))
        search_userid = request.POST.get('useremail', '')
        search_userpw = request.POST.get('userpw', '')
        search_name = request.POST.get('username', '')

        # data = JSONParser().parse(request)
        # search_userid = data['userid']
        # search_userpw = data['userpw']
        # search_name = data['name']
        print("id = ", search_userid, "pw = ", search_userpw, "name = ",search_name)
        data = {
            "name": search_name,
            "useremail": search_userid,
            "userpw": search_userpw
        }

        serializer = UsersSerializer(data=data)  # 파싱한 데이터를 serializer 에 넣음
        # => serializer 가 올바르면 객체 만듬
        if serializer.is_valid():
            serializer.save()
            return JsonResponse({'code': '0000', 'msg': '회원가입 성공입니다.'}, status=200)
        return JsonResponse({'code': '1001', 'msg': '회원가입 실패입니다.'}, status=400)


@csrf_exempt
def app_check_id(request):
    if request.method == 'POST':
        print("리퀘스트 로그" + str(request.body))
        input_id = request.POST.get('useremail', '')
        print(input_id)
        # data = JSONParser().parse(request)
        # input_id = data['userid']
        query_set = Users.objects.all()  # 모든 객체 다 읽어옴
        for user in query_set:
            print("입력:", input_id, " 기존: ", user.useremail)
            if input_id == user.useremail:
                return JsonResponse({'code': '1001', 'msg': 'ID 중복'}, status=200)
        return JsonResponse({'code': '0007', 'msg': 'ID 중복체크 통과'}, status=200)

@csrf_exempt
def list_delete(request):
    if request.method == 'POST':
        print("리퀘스트 로그" + str(request.body))
        data = json.loads(request.body)
        ##print("session : " + session_userid)
        likeName = data['likename']
        likelocation = data['location']
        id = data['useremail']
        print(likeName, likelocation,id)

        likelist = LikeList.objects.filter(useremail=id, likename=likeName, location=likelocation)
        likelist.delete()

