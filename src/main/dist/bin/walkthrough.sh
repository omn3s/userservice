#!/bin/bash


STATUS=$(curl -s -w "%{http_code}\n" --json '{"email":"fred@example.com", "password":"Foobar"}' http://localhost:7070/v1/register)

if [ "$STATUS" = "201" ]; then
    echo "User registered"
else
   echo "Registration failed"
   echo "$STATUS"
   exit
fi

TOKEN=$(curl -s --json '{"email":"fred@example.com", "password":"Foobar"}' http://localhost:7070/v1/login)

echo "Logged in and received token :"
echo "$TOKEN"
echo

PROFILE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:7070/v1/profile)

echo "Received profile:"
echo $PROFILE

