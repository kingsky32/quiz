import React from 'react';
import { AxiosResponse } from 'axios';
import { MutateOptions } from '@tanstack/react-query';
import {
  Button,
  Card,
  Col,
  Form as AntdForm,
  Input,
  message,
  Modal,
  Row,
  Space,
  Spin,
  Tree,
  TreeDataNode,
} from 'antd';
import Api from '../../../apis/api';

interface FormProps {
  id: Api.QuizCategory.Dto.Response['id'];
  onDelete: MutateOptions<
    AxiosResponse<any, any>,
    unknown,
    number
  >['onSuccess'];
}

function Form({ id, onDelete }: FormProps) {
  const quizCategoryQuery = Api.QuizCategory.Query.useQuizCategoryQuery(id);
  const updateQuizCategoryMutation =
    Api.QuizCategory.Mutation.useUpdateQuizCategoryMutation(id);
  const deleteQuizCategoryMutation =
    Api.QuizCategory.Mutation.useDeleteQuizCategoryMutation();
  if (!quizCategoryQuery.data) {
    return <Spin />;
  }
  return (
    <AntdForm
      initialValues={{
        name: quizCategoryQuery.data.data.name,
      }}
      onFinish={(values) => {
        updateQuizCategoryMutation.mutate(values, {
          onSuccess() {
            message.success('저장 완료');
          },
          onError(error) {
            message.error(error?.response?.data?.message);
          },
        });
      }}
    >
      <AntdForm.Item<Api.QuizCategory.Dto.Update>
        name="name"
        label="이름"
        rules={[{ required: true, message: '이름을 입력해주세요' }]}
      >
        <Input placeholder="이름을 입력해주세요" />
      </AntdForm.Item>
      <Row gutter={16} justify="end">
        <Col>
          <Button
            type="primary"
            htmlType="submit"
            loading={updateQuizCategoryMutation.isPending}
          >
            저장
          </Button>
        </Col>
        <Col>
          <Button
            type="primary"
            danger
            onClick={() => {
              deleteQuizCategoryMutation.mutate(id, {
                onSuccess(data, variables, context) {
                  message.success('삭제 성공');
                  onDelete?.(data, variables, context);
                },
                onError(error) {
                  message.error(error?.response?.data?.message);
                },
              });
            }}
            loading={deleteQuizCategoryMutation.isPending}
          >
            삭제
          </Button>
        </Col>
      </Row>
    </AntdForm>
  );
}

function Page() {
  const quizCategoriesQuery = Api.QuizCategory.Query.useQuizCategoriesQuery();
  const createQuizCategoryMutation =
    Api.QuizCategory.Mutation.useCreateQuizCategoryMutation();
  const [selectedQuizCategory, setSelectedQuizCategory] =
    React.useState<Api.QuizCategory.Dto.Response>();
  const treeData = React.useMemo<
    (TreeDataNode & { quizCategory: Api.QuizCategory.Dto.Response })[]
  >(() => {
    function recursive(
      quizCategory: Api.QuizCategory.Dto.Response,
    ): TreeDataNode & { quizCategory: Api.QuizCategory.Dto.Response } {
      return {
        quizCategory,
        key: `Quiz-Category-${quizCategory.id}`,
        title: (
          <Row gutter={16} justify="space-between" style={{ width: 350 }}>
            <Col>{quizCategory.name}</Col>
            <Col>
              <Button
                size="small"
                type="primary"
                onClick={(event) => {
                  event.stopPropagation();
                  const modal = Modal.info({
                    icon: null,
                    title: '소분류 생성',
                    styles: {
                      body: {
                        marginBottom: -12,
                      },
                    },
                    content: (
                      <AntdForm
                        onFinish={(values) => {
                          createQuizCategoryMutation.mutate(
                            { parentId: quizCategory.id, ...values },
                            {
                              onSuccess() {
                                modal.destroy();
                                message.success('생성 완료');
                              },
                              onError(error) {
                                message.error(error?.response?.data?.message);
                              },
                            },
                          );
                        }}
                      >
                        <AntdForm.Item<Api.QuizCategory.Dto.Create>
                          name="name"
                          label="이름"
                          rules={[
                            { required: true, message: '이름을 입력해주세요' },
                          ]}
                        >
                          <Input placeholder="이름을 입력해주세요" autoFocus />
                        </AntdForm.Item>
                        <Row gutter={16} justify="end">
                          <Col>
                            <Button
                              type="primary"
                              htmlType="submit"
                              loading={createQuizCategoryMutation.isPending}
                            >
                              저장
                            </Button>
                          </Col>
                        </Row>
                      </AntdForm>
                    ),
                    centered: true,
                    closable: true,
                    okButtonProps: {
                      style: {
                        display: 'none',
                      },
                    },
                  });
                }}
              >
                소분류 추가
              </Button>
            </Col>
          </Row>
        ),
        children: quizCategory.children?.map((child) => {
          return recursive(child);
        }),
      };
    }
    return (
      quizCategoriesQuery.data?.data.map((response) => {
        return recursive(response);
      }) ?? []
    );
  }, [quizCategoriesQuery.data]);

  if (!quizCategoriesQuery.data) {
    return <Spin />;
  }

  return (
    <Row gutter={16}>
      <Col span={12}>
        <Card style={{ height: 500 }}>
          <Space style={{ display: 'flex' }} direction="vertical">
            <Row gutter={16}>
              <Col>
                <Button
                  type="primary"
                  onClick={() => {
                    const modal = Modal.info({
                      icon: null,
                      title: '대분류 생성',
                      styles: {
                        body: {
                          marginBottom: -12,
                        },
                      },
                      content: (
                        <AntdForm
                          onFinish={(values) => {
                            createQuizCategoryMutation.mutate(values, {
                              onSuccess() {
                                modal.destroy();
                                message.success('생성 완료');
                              },
                              onError(error) {
                                message.error(error?.response?.data?.message);
                              },
                            });
                          }}
                        >
                          <AntdForm.Item<Api.QuizCategory.Dto.Create>
                            name="name"
                            label="이름"
                            rules={[
                              {
                                required: true,
                                message: '이름을 입력해주세요',
                              },
                            ]}
                          >
                            <Input
                              placeholder="이름을 입력해주세요"
                              autoFocus
                            />
                          </AntdForm.Item>
                          <Row gutter={16} justify="end">
                            <Col>
                              <Button
                                type="primary"
                                htmlType="submit"
                                loading={createQuizCategoryMutation.isPending}
                              >
                                저장
                              </Button>
                            </Col>
                          </Row>
                        </AntdForm>
                      ),
                      centered: true,
                      closable: true,
                      okButtonProps: {
                        style: {
                          display: 'none',
                        },
                      },
                    });
                  }}
                >
                  대분류 생성
                </Button>
              </Col>
            </Row>
            <Tree
              treeData={treeData}
              onSelect={(_selectedKeys, { node: { quizCategory } }) => {
                setSelectedQuizCategory(quizCategory);
              }}
              selectedKeys={
                selectedQuizCategory && [
                  `Quiz-Category-${selectedQuizCategory.id}`,
                ]
              }
            />
          </Space>
        </Card>
      </Col>
      <Col span={12}>
        <Card style={{ height: 500 }}>
          {selectedQuizCategory && (
            <Form
              key={`Form-${selectedQuizCategory.id}`}
              id={selectedQuizCategory.id}
              onDelete={() => {
                setSelectedQuizCategory(undefined);
              }}
            />
          )}
        </Card>
      </Col>
    </Row>
  );
}

export default Page;
