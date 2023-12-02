import 'react-quill/dist/quill.snow.css';
import React from 'react';
import type { DefaultOptionType } from 'rc-tree-select/lib/TreeSelect';
import { produce } from 'immer';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Button,
  Checkbox,
  Col,
  Form as AntdForm,
  Input,
  InputNumber,
  message,
  Row,
  Space,
  Spin,
  TreeSelect,
} from 'antd';
import ReactQuill from 'react-quill';
import Api from '../../../../apis/api';

function Form({
  id,
  quizCategory,
  title,
  content,
  soundFile,
  timeoutMs,
  isActive,
  quizHints,
  quizAnswers,
}: Api.Quiz.Dto.DetailResponse) {
  const navigate = useNavigate();
  const updateQuizMutation = Api.Quiz.Mutation.useUpdateQuizMutation(id);
  const deleteQuizMutation = Api.Quiz.Mutation.useDeleteQuizMutation();
  const quizCategoriesQuery = Api.QuizCategory.Query.useQuizCategoriesQuery();
  const fileUploadMutation = Api.File.Mutation.useFileUploadMutation();
  const [hints, setHints] = React.useState<Api.Quiz.Dto.CreateHint[]>(
    quizHints.map((response) => {
      return {
        name: response.name,
        content: response.content,
        exposedRemainTime: response.exposedRemainTime,
      };
    }),
  );
  const [answers, setAnswers] = React.useState<Api.Quiz.Dto.CreateAnswer[]>(
    quizAnswers.map((response) => {
      return {
        answer: response.answer,
      };
    }),
  );

  return (
    <AntdForm
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      style={{ maxWidth: 800 }}
      initialValues={{
        quizCategoryId: quizCategory.id,
        title,
        content,
        timeoutMs: timeoutMs / 1000,
        isActive,
      }}
      onFinish={(values) => {
        updateQuizMutation.mutate(
          {
            ...values,
            soundFileId: fileUploadMutation.data?.data.id ?? soundFile?.id,
            hints,
            answers,
            timeoutMs: values.timeoutMs * 1000,
          },
          {
            onSuccess() {
              message.success('수정 완료');
            },
            onError(error) {
              message.error(error?.response?.data?.message);
            },
          },
        );
      }}
    >
      <AntdForm.Item<Api.Quiz.Dto.Create>
        name="quizCategoryId"
        label="퀴즈 분류"
      >
        <TreeSelect
          showSearch
          placeholder="퀴즈 분류를 선택해주세요"
          style={{ width: 200 }}
          treeData={(() => {
            function recursive(
              // eslint-disable-next-line @typescript-eslint/no-shadow
              quizCategory: Api.QuizCategory.Dto.Response,
            ): DefaultOptionType {
              const option: DefaultOptionType = {
                title: quizCategory.name,
                value: quizCategory.id,
              };
              if (quizCategory.children) {
                option.children = quizCategory.children.map(recursive);
              }
              return option;
            }
            return quizCategoriesQuery.data?.data.map(recursive);
          })()}
          loading={quizCategoriesQuery.isLoading}
        />
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> name="title" label="제목">
        <Input style={{ width: 200 }} placeholder="제목을 입력해주세요" />
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> name="content" label="내용">
        <ReactQuill
          style={{ width: '100%', height: 250, marginBottom: 50 }}
          theme="snow"
        />
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> name="soundFileId" label="음성 파일">
        <Space direction="vertical">
          <Input
            style={{ width: 200 }}
            type="file"
            placeholder="파일을 선택해주세요"
            onChange={(event) => {
              const file = event.target.files?.item(0);
              if (file) {
                fileUploadMutation.mutate(file);
              }
            }}
          />
          {fileUploadMutation.isPending ? (
            <Spin />
          ) : fileUploadMutation.data ? (
            // eslint-disable-next-line jsx-a11y/media-has-caption
            <audio controls>
              <source
                src={Api.File.getFilePath(fileUploadMutation.data.data.id)}
                type={fileUploadMutation.data.data.contentType}
              />
            </audio>
          ) : (
            // eslint-disable-next-line jsx-a11y/media-has-caption
            <audio controls>
              <source
                src={Api.File.getFilePath(soundFile.id)}
                type={soundFile.contentType}
              />
            </audio>
          )}
        </Space>
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> name="timeoutMs" label="문제 시간">
        <InputNumber
          onKeyDown={(event) => {
            if (event.key.length < 2 && /[^0-9]/g.test(event.key)) {
              event.preventDefault();
            }
          }}
          formatter={(value) => {
            return `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
          }}
          parser={(value) => {
            return value?.replace(/\$\s?|(,*)/g, '') ?? '';
          }}
          suffix="초"
        />
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create>
        name="isActive"
        label="사용 여부"
        valuePropName="checked"
      >
        <Checkbox>사용 여부</Checkbox>
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> name="hints" label="힌트">
        <Space style={{ display: 'flex' }} direction="vertical">
          {hints.map((hint, hintIndex, hintArray) => {
            return (
              <Row
                key={`Hint-${hintIndex}`}
                gutter={16}
                align="middle"
                wrap={false}
              >
                <Col>
                  <Space>
                    <Input
                      placeholder="타입를 입력해주세요"
                      style={{ width: 150 }}
                      value={hint.name}
                      onChange={(event) => {
                        setHints(
                          produce((draft) => {
                            draft[hintIndex].name = event.target.value;
                          }),
                        );
                      }}
                    />
                    <Input
                      placeholder="내용을 입력해주세요"
                      style={{ width: 200 }}
                      value={hint.content}
                      onChange={(event) => {
                        setHints(
                          produce((draft) => {
                            draft[hintIndex].content = event.target.value;
                          }),
                        );
                      }}
                    />
                    <InputNumber
                      style={{ width: 75 }}
                      onKeyDown={(event) => {
                        if (event.key.length < 2 && /[^0-9]/g.test(event.key)) {
                          event.preventDefault();
                        }
                      }}
                      formatter={(value) => {
                        return `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                      }}
                      parser={(value) => {
                        return `${value}`?.replace(/\$\s?|(,*)/g, '') ?? '';
                      }}
                      value={`${hint.exposedRemainTime}` as string}
                      onChange={(value) => {
                        setHints(
                          produce((draft) => {
                            draft[hintIndex].exposedRemainTime = Number(value);
                          }),
                        );
                      }}
                      suffix="초"
                    />
                  </Space>
                </Col>
                <Col>
                  <Space>
                    {hintArray.length > 1 && (
                      <Button
                        type="primary"
                        danger
                        onClick={() => {
                          setHints(
                            produce((draft) => {
                              draft.splice(hintIndex, 1);
                            }),
                          );
                        }}
                      >
                        삭제
                      </Button>
                    )}
                    {hintIndex === hintArray.length - 1 && (
                      <Button
                        type="primary"
                        onClick={() => {
                          setHints(
                            produce((draft) => {
                              draft.push({
                                name: '',
                                content: '',
                                exposedRemainTime: 0,
                              });
                            }),
                          );
                        }}
                      >
                        추가
                      </Button>
                    )}
                  </Space>
                </Col>
              </Row>
            );
          })}
        </Space>
      </AntdForm.Item>
      <AntdForm.Item<Api.Quiz.Dto.Create> label="답">
        <Space style={{ display: 'flex' }} direction="vertical">
          {answers.map((answer, answerIndex, answerArray) => {
            return (
              <Row
                key={`Answer-${answer}`}
                gutter={16}
                align="middle"
                wrap={false}
              >
                <Col>
                  <Input
                    placeholder="답을 입력해주세요"
                    style={{ width: 200 }}
                    value={answer.answer}
                    onChange={(event) => {
                      setAnswers(
                        produce((draft) => {
                          draft[answerIndex].answer = event.target.value;
                        }),
                      );
                    }}
                  />
                </Col>
                <Col>
                  <Space>
                    {answerArray.length > 1 && (
                      <Button
                        type="primary"
                        danger
                        onClick={() => {
                          setAnswers(
                            produce((draft) => {
                              draft.splice(answerIndex, 1);
                            }),
                          );
                        }}
                      >
                        삭제
                      </Button>
                    )}
                    {answerIndex === answerArray.length - 1 && (
                      <Button
                        type="primary"
                        onClick={() => {
                          setAnswers(
                            produce((draft) => {
                              draft.push({
                                answer: '',
                              });
                            }),
                          );
                        }}
                      >
                        추가
                      </Button>
                    )}
                  </Space>
                </Col>
              </Row>
            );
          })}
        </Space>
      </AntdForm.Item>
      <Row justify="end" align="middle" gutter={16}>
        <Col>
          <Button
            onClick={() => {
              navigate('/admin/quiz/management');
            }}
          >
            목록
          </Button>
        </Col>
        <Col>
          <Button
            type="primary"
            danger
            loading={deleteQuizMutation.isPending}
            onClick={() => {
              deleteQuizMutation.mutate(id);
            }}
          >
            삭제
          </Button>
        </Col>
        <Col>
          <Button
            type="primary"
            htmlType="submit"
            loading={updateQuizMutation.isPending}
          >
            수정
          </Button>
        </Col>
      </Row>
    </AntdForm>
  );
}

function Page() {
  const params = useParams();
  const quizQuery = Api.Quiz.Query.useQuizQuery(Number(params.id));

  if (!quizQuery.data) {
    return <Spin />;
  }

  return <Form {...quizQuery.data.data} />;
}

export default Page;
