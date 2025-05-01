package ru.practicum.shareit.server.item.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.server.item.Comment;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.user.User;

@DisplayName("Comment Mapper Implementation Tests")
class CommentMapperImplTest {

    private CommentMapperImpl commentMapper;

    private Comment testComment;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        commentMapper = new CommentMapperImpl();

        User testAuthor = new User();
        testAuthor.setId(1L);
        testAuthor.setName("Author Name");
        testAuthor.setEmail("author@example.com");

        Item testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setAvailable(true);
        User owner = new User();
        owner.setId(2L);
        testItem.setOwner(owner);

        testTimestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        testComment = new Comment();
        testComment.setId(100L);
        testComment.setText("This is a test comment.");
        testComment.setItem(testItem);
        testComment.setAuthor(testAuthor);
        testComment.setCreatedAt(testTimestamp);
    }

    @Test
    @DisplayName("mapToDto should map Comment to CommentDto correctly")
    void mapToDto_whenCommentIsValid_shouldReturnCorrectCommentDto() {
        CommentDto commentDto = commentMapper.mapToDto(testComment);

        assertThat("Mapped DTO should not be null", commentDto, is(notNullValue()));
        assertThat("Mapped DTO should have correct ID", commentDto,
            hasProperty("id", equalTo(100L)));
        assertThat("Mapped DTO should have correct text", commentDto,
            hasProperty("text", equalTo("This is a test comment.")));
        assertThat("Mapped DTO should have correct author name", commentDto,
            hasProperty("authorName", equalTo("Author Name")));
        assertThat("Mapped DTO should have correct creation timestamp as String", commentDto,
            hasProperty("created", equalTo(testTimestamp.toString())));
    }

    @Test
    @DisplayName("mapToComment should map NewCommentDto to Comment correctly")
    void mapToComment_whenNewCommentDtoIsValid_shouldReturnCorrectComment() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("New comment text.");

        Comment comment = commentMapper.mapToComment(newCommentDto);

        assertThat("Mapped Comment should not be null", comment, is(notNullValue()));
        assertThat("Mapped Comment should have correct text", comment,
            hasProperty("text", equalTo("New comment text.")));
        assertThat("Mapped Comment ID should be null", comment, hasProperty("id", is(nullValue())));
        assertThat("Mapped Comment Item should be null", comment,
            hasProperty("item", is(nullValue())));
        assertThat("Mapped Comment Author should be null", comment,
            hasProperty("author", is(nullValue())));
        assertThat("Mapped Comment CreatedAt should be null", comment,
            hasProperty("createdAt", is(nullValue())));
    }

    @Test
    @DisplayName("mapToComment should handle empty text in NewCommentDto")
    void mapToComment_whenNewCommentDtoHasEmptyText_shouldReturnCommentWithEmptyText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("");

        Comment comment = commentMapper.mapToComment(newCommentDto);

        assertThat("Mapped Comment should not be null", comment, is(notNullValue()));
        assertThat("Mapped Comment should have empty text", comment,
            hasProperty("text", equalTo("")));
    }

    @Test
    @DisplayName("mapToComment should handle null text in NewCommentDto")
    void mapToComment_whenNewCommentDtoHasNullText_shouldReturnCommentWithNullText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText(null); // DTO allows null text before validation

        Comment comment = commentMapper.mapToComment(newCommentDto);

        assertThat("Mapped Comment should not be null", comment, is(notNullValue()));
        assertThat("Mapped Comment should have null text", comment,
            hasProperty("text", is(nullValue())));
    }
}