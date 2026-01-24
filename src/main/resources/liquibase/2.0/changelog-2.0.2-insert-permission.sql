insert into permissions(url, http_method, description)
values ('/payment/kapitalbank', 'POST', 'Create order');

insert into roles_permissions(role_id, permission_id)
values (1, 1);