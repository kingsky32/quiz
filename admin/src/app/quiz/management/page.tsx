import React from 'react';
import dayjs from 'dayjs';
import { Button, Col, message, Row, Space, Table } from 'antd';
import { useNavigate } from 'react-router-dom';
import Api from '../../../apis/api';

interface ActionColProps {
  id: Api.Quiz.Dto.Response['id'];
}

function ActionCol({ id }: ActionColProps) {
  const navigate = useNavigate();
  const deleteQuizMutation = Api.Quiz.Mutation.useDeleteQuizMutation();
  return (
    <Row gutter={16} justify="center" align="middle">
      <Col>
        <Button
          type="primary"
          onClick={() => {
            navigate(`/admin/quiz/management/${id}`);
          }}
        >
          수정
        </Button>
      </Col>
      <Col>
        <Button
          type="primary"
          danger
          onClick={() => {
            deleteQuizMutation.mutate(id, {
              onSuccess() {
                message.success('삭제 성공');
              },
              onError(error) {
                message.error(error?.response?.data?.message);
              },
            });
          }}
        >
          삭제
        </Button>
      </Col>
    </Row>
  );
}

function Page() {
  const [params, setParams] = React.useState<Api.Quiz.Dto.Request>({
    limit: 20,
    page: 1,
  });
  const quizzesQuery = Api.Quiz.Query.useQuizzesQuery(params);
  const navigate = useNavigate();

  return (
    <Space style={{ display: 'flex' }} direction="vertical" size="middle">
      <Row justify="end" gutter={16} align="middle">
        <Col>
          <Button
            type="primary"
            onClick={() => {
              navigate('/admin/quiz/management/create');
            }}
          >
            퀴즈 등록
          </Button>
        </Col>
      </Row>
      <Table
        dataSource={quizzesQuery.data?.data.results}
        columns={[
          {
            title: 'No',
            width: 50,
            align: 'center',
            render: (_value, _record, index) => {
              return (
                (quizzesQuery.data?.data.totalResults ?? 0) -
                ((quizzesQuery.data?.data.page ?? 0) - 1) *
                  (quizzesQuery.data?.data.limit ?? 0) -
                index
              );
            },
          },
          {
            title: '분류',
            dataIndex: 'quizCategory',
            align: 'center',
            width: 150,
            render: (value) => {
              return value.name;
            },
          },
          { title: '제목', dataIndex: 'title', align: 'center', width: 200 },
          {
            title: '사용 여부',
            dataIndex: 'isActive',
            align: 'center',
            width: 200,
            render: (value) => {
              return value ? 'Y' : 'N';
            },
          },
          {
            title: '생성자',
            dataIndex: 'createdBy',
            align: 'center',
            width: 200,
            render: (value) => {
              return value.name;
            },
          },
          {
            title: '생성일',
            dataIndex: 'createdAt',
            align: 'center',
            width: 200,
            render: (value) => {
              return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
            },
          },
          {
            title: '수정일',
            dataIndex: 'updatedAt',
            align: 'center',
            width: 200,
            render: (value) => {
              return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
            },
          },
          {
            title: '액션',
            dataIndex: 'id',
            align: 'center',
            width: 200,
            render: (value) => {
              return <ActionCol id={value} />;
            },
          },
        ]}
        rowKey="id"
        loading={quizzesQuery.isPending}
        pagination={{
          current: quizzesQuery.data?.data.page,
          total: quizzesQuery.data?.data.totalResults,
          pageSize: quizzesQuery.data?.data.limit,
          position: ['bottomCenter'],
          showSizeChanger: true,
          onChange: (page, pageSize) => {
            setParams((prevState) => {
              return {
                ...prevState,
                page,
                limit: pageSize,
              };
            });
          },
        }}
      />
    </Space>
  );
}

export default Page;
