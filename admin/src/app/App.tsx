import React from 'react';
import { Link, Route, Routes, useLocation } from 'react-router-dom';
import {
  Breadcrumb,
  Button,
  Divider,
  Form,
  Input,
  Layout,
  Menu,
  MenuProps,
  message,
  Space,
  theme,
  Typography,
} from 'antd';
import routes, { Route as IRoute } from './routes';
import Api from '../apis/api';

function LoginForm() {
  const loginMutation = Api.Auth.Mutation.useLoginMutation();
  return (
    <Form
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      style={{ maxWidth: 600 }}
      onFinish={(values) => {
        loginMutation.mutate(values, {
          onError(error) {
            message.error(error?.response?.data?.message);
          },
        });
      }}
    >
      <Form.Item<Api.Auth.AuthDto.Login>
        label="Username"
        name="username"
        rules={[
          {
            required: true,
            message: 'Please input your username!',
          },
        ]}
      >
        <Input />
      </Form.Item>
      <Form.Item<Api.Auth.AuthDto.Login>
        label="Password"
        name="password"
        rules={[
          {
            required: true,
            message: 'Please input your password!',
          },
        ]}
      >
        <Input.Password />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
        <Button
          type="primary"
          htmlType="submit"
          loading={loginMutation.isPending}
        >
          Submit
        </Button>
      </Form.Item>
    </Form>
  );
}

function App() {
  const meQuery = Api.Auth.Query.useMeQuery();
  const {
    token: { colorBgContainer },
  } = theme.useToken();
  const location = useLocation();
  const selectedKeys = React.useMemo(() => {
    return location.pathname
      .split('/')
      .reduce<string[]>((previousValue, currentValue, currentIndex) => {
        if (currentIndex > 0) {
          return [
            ...previousValue,
            `${previousValue[currentIndex - 2] ?? ''}/${currentValue}`,
          ];
        }
        return previousValue;
      }, []);
  }, [location]);
  const menuItems = React.useMemo(() => {
    return routes.reduce<MenuProps['items']>((previousValue, currentValue) => {
      if (currentValue.menu) {
        const children = currentValue.children?.reduce<MenuProps['items']>( // eslint-disable-next-line @typescript-eslint/no-shadow
          (previousValue, currentValue) => {
            if (currentValue.menu) {
              previousValue?.push({
                key: currentValue.path,
                label: <Link to={currentValue.path}>{currentValue.name}</Link>,
              });
            }
            return previousValue;
          },
          [],
        );
        previousValue?.push({
          key: currentValue.path,
          label:
            (children?.length ?? 0) > 0 ? (
              currentValue.name
            ) : (
              <Link to={currentValue.path}>{currentValue.name}</Link>
            ),
          children: (children?.length ?? 0) > 0 ? children : undefined,
        });
      }
      return previousValue;
    }, []);
  }, []);
  const breadCrumbItems = React.useMemo(() => {
    const $routes: IRoute[] = [];
    const maxDepth = location.pathname.split('/').length - 1;

    function recursive(route: IRoute, depth: number) {
      if (maxDepth > depth) {
        if ($routes.length <= depth) {
          if (
            route.path.includes(':') ||
            location.pathname.startsWith(route.path)
          ) {
            $routes.push(route);
            route.children?.forEach((child) => {
              recursive(child, depth + 1);
            });
          }
        }
      }
    }

    routes.forEach((route) => {
      recursive(route, 0);
    });
    return $routes.map(($route) => {
      return {
        title: $route.name,
        auth: $route.auth,
      };
    });
  }, [location.pathname]);
  const renderRoutes = React.useCallback(() => {
    function recursive(route: IRoute): React.ReactNode[] {
      return [
        <Route
          key={`Route-${route.path}`}
          path={route.path}
          element={route.element}
        />,
        ...(route.children?.reduce<React.ReactNode[]>(
          (previousValue, currentValue) => {
            previousValue.push(
              <Route
                key={`Route-${currentValue.path}`}
                path={currentValue.path}
                element={currentValue.element}
              />,
            );
            currentValue.children?.forEach((child) => {
              previousValue.push(...(recursive(child) ?? []));
            });
            return previousValue;
          },
          [],
        ) ?? []),
      ];
    }

    return routes.reduce<React.ReactNode[]>((previousValue, currentValue) => {
      return [...previousValue, ...(recursive(currentValue) ?? [])];
    }, []);
  }, []);

  return (
    <Layout style={{ height: '100vh' }}>
      <Layout.Sider breakpoint="lg" collapsedWidth="0">
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={selectedKeys}
          items={menuItems}
        />
      </Layout.Sider>
      <Layout>
        <Layout.Header style={{ padding: 0, background: colorBgContainer }} />
        <Layout.Content style={{ margin: '24px 16px 0' }}>
          <div
            style={{
              minHeight: '100%',
              padding: 24,
              background: colorBgContainer,
            }}
          >
            <Space
              style={{ display: 'flex', overflow: 'hidden' }}
              direction="vertical"
              size="large"
            >
              {breadCrumbItems.length > 0 && (
                <>
                  <Space direction="vertical">
                    <Breadcrumb items={breadCrumbItems} />
                    <Typography.Title level={2} style={{ marginBottom: 0 }}>
                      {breadCrumbItems[breadCrumbItems.length - 1]?.title}
                    </Typography.Title>
                  </Space>
                  <Divider />
                </>
              )}
              {meQuery.data ? <Routes>{renderRoutes()}</Routes> : <LoginForm />}
            </Space>
          </div>
        </Layout.Content>
        <Layout.Footer style={{ textAlign: 'center' }}>
          Ant Design ©2023 Created by Ant UED
        </Layout.Footer>
      </Layout>
    </Layout>
  );
}

export default App;
